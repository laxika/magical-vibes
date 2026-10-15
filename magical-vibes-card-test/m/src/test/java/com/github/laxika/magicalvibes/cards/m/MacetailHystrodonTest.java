package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.b.BranchsnapLorian;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MacetailHystrodon.class, FugitiveWizard.class, BranchsnapLorian.class})
class MacetailHystrodonTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards Macetail Hystrodon and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MacetailHystrodon()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Macetail Hystrodon");
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cycling cannot be activated without three mana")
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new MacetailHystrodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Macetail Hystrodon");
    }

    @Test
    void cyclingDiscardsAsACostAndDrawsOnlyOnResolution() {
        harness.setHand(player1, List.of(new MacetailHystrodon()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Macetail Hystrodon");
        harness.assertInGraveyard(player1, "Macetail Hystrodon");
        harness.assertNotInHand(player1, "Fugitive Wizard");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Fugitive Wizard");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cyclingCanBeActivatedDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new MacetailHystrodon()));
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Macetail Hystrodon");
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    void hasteAllowsAttackingTheTurnItEntersAndDealsDamageOnlyOnce() {
        harness.castFromHand(player1, new MacetailHystrodon(), "{6}{R}");
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    void firstStrikeKillsALethalBlockerBeforeItCanDealDamage() {
        addCreatureReady(player1, new MacetailHystrodon());
        addCreatureReady(player2, new BranchsnapLorian());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Macetail Hystrodon");
        harness.assertInGraveyard(player2, "Branchsnap Lorian");
        harness.assertLife(player2, 20);
    }
}
