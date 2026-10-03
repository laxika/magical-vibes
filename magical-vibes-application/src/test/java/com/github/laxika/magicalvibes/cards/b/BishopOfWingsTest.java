package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.d.DawningAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BishopOfWings.class, GreenwoodSentinel.class, Murder.class, DawningAngel.class, ArcaneAdaptation.class})
class BishopOfWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 4 life when an Angel you control enters")
    void gainsLifeWhenAllyAngelEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.setHand(player1, List.of(new DawningAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(28);
    }

    @Test
    @DisplayName("Does not gain life when a non-Angel creature enters")
    void doesNotGainLifeWhenNonAngelEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.enterBattlefieldAndReturn(player1, new GreenwoodSentinel());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Creates a 1/1 white flying Spirit when an Angel you control dies")
    void createsSpiritWhenAllyAngelDies() {
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.addToBattlefield(player1, new DawningAngel());

        destroyWithMurder(player2, player1, "Dawning Angel");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void opponentAngelEnteringDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.enterBattlefieldAndReturn(player2, new DawningAngel());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void opponentAngelDyingDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.addToBattlefield(player2, new DawningAngel());
        destroyWithMurder(player1, player2, "Dawning Angel");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void nonAngelDyingDoesNotCreateSpirit() {
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        destroyWithMurder(player2, player1, "Greenwood Sentinel");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void bishopEnteringAsAngelTriggersItsOwnLifeGain() {
        harness.setLife(player1, 20);
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.ANGEL);
        harness.enterBattlefieldAndReturn(player1, new BishopOfWings());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void bishopDyingAsAngelCreatesSpirit() {
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.ANGEL);
        harness.addToBattlefield(player1, new BishopOfWings());
        destroyWithMurder(player2, player1, "Bishop of Wings");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    private void destroyWithMurder(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
