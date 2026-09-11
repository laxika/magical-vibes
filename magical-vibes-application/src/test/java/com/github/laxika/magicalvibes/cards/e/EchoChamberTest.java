package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.r.RootwaterHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EchoChamber.class, LowlandGiant.class, RootwaterHunter.class})
class EchoChamberTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent picks one of their creatures and the controller gets a hasty token copy")
    void opponentChoosesCreatureToCopy() {
        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent hunter = harness.addToBattlefieldAndReturn(player2, new RootwaterHunter());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new LowlandGiant());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(hunter.getId(), giant.getId());

        harness.handlePermanentChosen(player2, hunter.getId());
        harness.passBothPriorities();

        Permanent token = tokenCopy();
        // The token is the controller's, not the choosing opponent's.
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(token), null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("The opponent chooses the sole legal target during activation")
    void singleCreatureIsChosenDuringActivation() {
        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent hunter = harness.addToBattlefieldAndReturn(player2, new RootwaterHunter());

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handlePermanentChosen(player2, hunter.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(tokenCopy()).isNotNull();
    }

    @Test
    @DisplayName("The ability cannot be activated when the opponent controls no creature")
    void noCreatureToCopy() {
        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    @Test
    @DisplayName("The token is exiled at the beginning of the next end step")
    void tokenExiledAtEndStep() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent hunter = harness.addToBattlefieldAndReturn(player2, new RootwaterHunter());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player2, hunter.getId());
        harness.passBothPriorities();
        assertThat(tokenCopy()).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot be activated at instant speed")
    void cannotActivateAtInstantSpeed() {
        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new RootwaterHunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent tokenCopy() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No token copy was created"));
    }

    private void setupEchoChamberOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new EchoChamber());
        findPermanent(player1, "Echo Chamber").setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
