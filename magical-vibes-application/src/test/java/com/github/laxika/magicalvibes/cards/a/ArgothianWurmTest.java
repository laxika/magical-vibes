package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArgothianWurmTest extends BaseCardTest {

    private void castAndResolveToChoice() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ArgothianWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining leaves the Wurm and land on the battlefield")
    void decliningKeepsBothPermanents() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Argothian Wurm");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Sacrificing a land puts the Wurm on top of its owner's library")
    void sacrificingLandTucksWurm() {
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Argothian Wurm");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Argothian Wurm");
    }

    @Test
    @DisplayName("Remaining players still receive the choice after a land is sacrificed")
    void remainingPlayersStillGetChoice() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ArgothianWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Argothian Wurm");
    }

    @Test
    @DisplayName("An accepting player with multiple lands chooses which land to sacrifice")
    void choosesLandWhenSeveralAreControlled() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ArgothianWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }
}
