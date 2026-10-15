package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArgothianWurm.class, Forest.class, Rescind.class})
class ArgothianWurmTest extends BaseCardTest {

    private void castAndResolveToChoice() {
        harness.addToBattlefield(player2, new Forest());
        castAndResolveWurm();
    }

    private void castAndResolveWurm() {
        harness.castFromHand(player1, new ArgothianWurm(), "{3}{G}");
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
        castAndResolveWurm();

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
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAndResolveWurm();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, secondLand.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(firstLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondLand.getCard());
        harness.assertNotOnBattlefield(player1, "Argothian Wurm");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Argothian Wurm");
    }

    @Test
    @DisplayName("All players can decline the land sacrifice")
    void allPlayersDecline() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castAndResolveWurm();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Argothian Wurm");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(1);
    }

    @Test
    @DisplayName("No player is offered the choice when no player controls a land")
    void noChoiceWithoutLands() {
        castAndResolveWurm();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Argothian Wurm");
    }

    @Test
    @DisplayName("Both players can sacrifice even after the Wurm goes to its library")
    void bothPlayersCanSacrifice() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castAndResolveWurm();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Argothian Wurm");
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Argothian Wurm"))
                .hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Argothian Wurm");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A player may sacrifice a land after the Wurm leaves before its trigger resolves")
    void sacrificeDoesNotMoveWurmFromHand() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new ArgothianWurm(), "{3}{G}");
        harness.passBothPriorities();
        Permanent wurm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Argothian Wurm"))
                .findFirst().orElseThrow();
        harness.setHand(player2, List.of(new Rescind()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player2, 0, wurm.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInHand(player1, "Argothian Wurm");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Argothian Wurm"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
