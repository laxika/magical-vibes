package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.c.ChimneyRabble;
import com.github.laxika.magicalvibes.cards.p.PaladinOfPredation;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HexgoldSlash.class, ChimneyRabble.class, CrawlingChorus.class, PaladinOfPredation.class})
class HexgoldSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a creature without toxic")
    void dealsTwoDamageToCreatureWithoutToxic() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChimneyRabble());
        castHexgoldSlash(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Chimney Rabble");
    }

    @Test
    @DisplayName("Deals 4 damage to a creature with toxic instead")
    void dealsFourDamageToCreatureWithToxicInstead() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        target.setToughnessModifier(2);
        castHexgoldSlash(target);

        harness.assertNotOnBattlefield(player2, "Crawling Chorus");
        harness.assertInGraveyard(player2, "Crawling Chorus");
    }

    @Test
    @DisplayName("Deals exactly 4 damage regardless of the toxic value")
    void dealsExactlyFourDamageToToxicSixCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinOfPredation());
        castHexgoldSlash(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Paladin of Predation");
    }

    @Test
    @DisplayName("Checks toxic gained after casting at resolution")
    void checksToxicGainedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChimneyRabble());
        prepareHexgoldSlash();
        harness.castInstant(player1, 0, target.getId());
        target.getPersistentGrantedKeywords().add(Keyword.TOXIC);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Chimney Rabble");
    }

    @Test
    @DisplayName("Deals only 2 damage if the target loses toxic before resolution")
    void checksToxicLostBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinOfPredation());
        prepareHexgoldSlash();
        harness.castInstant(player1, 0, target.getId());
        target.getRemovedKeywords().add(Keyword.TOXIC);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Paladin of Predation");
    }

    @Test
    @DisplayName("Can damage a creature controlled by its caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PaladinOfPredation());
        castHexgoldSlash(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Paladin of Predation");
    }

    private void prepareHexgoldSlash() {
        harness.setHand(player1, List.of(new HexgoldSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void castHexgoldSlash(Permanent target) {
        prepareHexgoldSlash();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
