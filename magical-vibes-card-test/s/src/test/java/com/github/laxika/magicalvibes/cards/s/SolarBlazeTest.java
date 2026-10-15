package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Banehound;
import com.github.laxika.magicalvibes.cards.c.CharityExtractor;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.Justice;
import com.github.laxika.magicalvibes.cards.l.LeylineProwler;
import com.github.laxika.magicalvibes.cards.w.WallOfRunes;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolarBlaze.class, GiantSpider.class, HillGiant.class, Justice.class, WallOfSwords.class,
        Banehound.class, CharityExtractor.class, LeylineProwler.class, WallOfRunes.class, SerraAvatar.class})
class SolarBlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature deals damage to itself equal to its power")
    void eachCreatureDealsItsPowerToItself() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfSwords());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new SolarBlaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Wall of Swords");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Each creature is the source of its own damage")
    void eachCreatureIsItsOwnDamageSource() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new Justice());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SolarBlaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Lifelink benefits each creature's controller even when self-damage is lethal")
    void lifelinkBenefitsBothControllers() {
        harness.addToBattlefield(player1, new Banehound());
        Permanent extractor = harness.addToBattlefieldAndReturn(player2, new CharityExtractor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SolarBlaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertInGraveyard(player1, "Banehound");
        harness.assertOnBattlefield(player2, "Charity Extractor");
        assertThat(extractor.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature's deathtouch makes its nonlethal self-damage lethal")
    void deathtouchDestroysItsOwnSource() {
        harness.addToBattlefield(player2, new LeylineProwler());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SolarBlaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player2, "Leyline Prowler");
        harness.assertNotOnBattlefield(player2, "Leyline Prowler");
    }

    @Test
    @DisplayName("Zero-power creatures deal no damage to themselves")
    void zeroPowerCreatureSurvivesUndamaged() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfRunes());
        harness.setHand(player1, List.of(new SolarBlaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Wall of Runes");
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("All self-damage amounts are determined before simultaneous lifelink changes power")
    void simultaneousLifelinkDoesNotIncreaseOtherCreaturesDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Banehound());
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new SerraAvatar());
        harness.setHand(player1, List.of(new SolarBlaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 21);
        assertThat(avatar.getMarkedDamage()).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Serra Avatar");
        harness.assertInGraveyard(player1, "Banehound");
    }
}
