package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Soulshriek.class, BayFalcon.class, DarkRitual.class, CharcoalDiamond.class})
class SoulshriekTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +X/+0 where X is the number of creature cards in your graveyard")
    void boostsByCreatureCardsInGraveyard() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setGraveyard(player1, List.of(new BayFalcon(), new BayFalcon()));

        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        int basePower = gqs.getEffectivePower(gd, falcon);
        int baseToughness = gqs.getEffectiveToughness(gd, falcon);

        harness.castAndResolveInstant(player1, 0, falcon.getId());

        Permanent after = gqs.findPermanentById(gd, falcon.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Gives no boost with an empty graveyard")
    void noBoostWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        int basePower = gqs.getEffectivePower(gd, falcon);

        harness.castAndResolveInstant(player1, 0, falcon.getId());

        assertThat(gqs.getEffectivePower(gd, gqs.findPermanentById(gd, falcon.getId()))).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Counts only creature cards in your graveyard")
    void countsOnlyOwnCreatureCards() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setGraveyard(player1, List.of(new BayFalcon(), new DarkRitual()));
        harness.setGraveyard(player2, List.of(new BayFalcon(), new BayFalcon()));

        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        int basePower = gqs.getEffectivePower(gd, falcon);

        harness.castAndResolveInstant(player1, 0, falcon.getId());

        assertThat(gqs.getEffectivePower(gd, gqs.findPermanentById(gd, falcon.getId())))
                .isEqualTo(basePower + 1);
    }

    @Test
    @DisplayName("Counts creature cards in the graveyard as the spell resolves")
    void countsAtResolution() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setGraveyard(player1, List.of(new BayFalcon()));

        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());
        int basePower = gqs.getEffectivePower(gd, falcon);

        harness.castInstant(player1, 0, falcon.getId());
        harness.setGraveyard(player1, List.of(new BayFalcon(), new BayFalcon()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gqs.findPermanentById(gd, falcon.getId())))
                .isEqualTo(basePower + 2);
    }

    @Test
    @DisplayName("The boosted creature is sacrificed at the beginning of the next end step")
    void sacrificesTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent falcon = harness.addToBattlefieldAndReturn(player1, new BayFalcon());

        harness.castAndResolveInstant(player1, 0, falcon.getId());

        harness.assertOnBattlefield(player1, "Bay Falcon");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        harness.assertInGraveyard(player1, "Bay Falcon");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent opponentFalcon = harness.addToBattlefieldAndReturn(player2, new BayFalcon());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentFalcon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent diamond = harness.addToBattlefieldAndReturn(player1, new CharcoalDiamond());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, diamond.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }
}
