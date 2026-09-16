package com.github.laxika.magicalvibes.cards.p;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ProwlingPangolin.class, ElvishWarrior.class})
class ProwlingPangolinTest extends BaseCardTest {

    private void castAndResolve() {
        harness.castFromHand(player1, new ProwlingPangolin(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining leaves Prowling Pangolin and the creatures on the battlefield")
    void decliningKeepsPermanents() {
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Prowling Pangolin");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Accepting sacrifices exactly two creatures and Prowling Pangolin")
    void acceptingSacrificesTwoCreaturesAndSource() {
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        castAndResolve();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player1, "Prowling Pangolin");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The accepting player may choose Prowling Pangolin as one of the two creatures")
    void acceptingPlayerMayChooseSourceCreature() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Prowling Pangolin");
        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An accepting player chooses which two creatures to sacrifice")
    void choosesTwoCreaturesWhenMoreThanTwoAreAvailable() {
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        castAndResolve();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> warriorIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .toList();
        harness.handleMultiplePermanentsChosen(player2, warriorIds.subList(0, 2));

        harness.assertNotOnBattlefield(player1, "Prowling Pangolin");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Later players still receive the choice after an earlier player accepts")
    void laterPlayersStillReceiveChoice() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        castAndResolve();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> warriorIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Elvish Warrior"))
                .map(Permanent::getId)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, warriorIds);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Prowling Pangolin");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No player is offered the choice without two creatures")
    void noChoiceWithoutTwoCreatures() {
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Prowling Pangolin");
    }
}
