package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RishadanCutpurse.class, FreshVolunteers.class, Island.class})
class RishadanCutpurseTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent may pay {1} to keep their permanents")
    void opponentMayPayToKeepPermanent() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castRishadanCutpurse();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent who declines sacrifices a permanent of their choice")
    void opponentDeclinesAndSacrificesPermanent() {
        giveManaSource(player2);
        harness.addToBattlefield(player2, new FreshVolunteers());
        castRishadanCutpurse();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("An opponent with no permanents does not need to pay")
    void opponentWithNoPermanentsDoesNotNeedToPay() {
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        castRishadanCutpurse();

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent chooses which permanent to sacrifice")
    void opponentChoosesPermanentToSacrifice() {
        giveManaSource(player2);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        castRishadanCutpurse();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("An opponent can activate a land mana ability while choosing whether to pay")
    void opponentCanGenerateManaDuringPaymentChoice() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addToBattlefield(player2, new FreshVolunteers());
        castRishadanCutpurse();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.tapPermanent(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(island.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent may choose a land instead of a creature to sacrifice")
    void opponentCanChooseLandToSacrifice() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addToBattlefield(player2, new FreshVolunteers());
        castRishadanCutpurse();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMultiplePermanentsChosen(player2, List.of(island.getId()));

        harness.assertInGraveyard(player2, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        harness.assertOnBattlefield(player1, "Rishadan Cutpurse");
    }

    @Test
    @DisplayName("Declining payment leaves available mana unspent")
    void decliningPaymentDoesNotSpendMana() {
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addMana(player2, ManaColor.BLUE, 1);
        castRishadanCutpurse();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Fresh Volunteers");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Rishadan Cutpurse");
    }

    @Test
    @DisplayName("The trigger affects the opponent of its controller, regardless of active player")
    void triggerUsesItsControllerToDetermineOpponent() {
        giveManaSource(player1);
        harness.addToBattlefield(player1, new FreshVolunteers());
        harness.enterBattlefieldAndReturn(player2, new RishadanCutpurse());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Fresh Volunteers");
        harness.assertOnBattlefield(player2, "Rishadan Cutpurse");
        harness.assertNotInGraveyard(player2, "Rishadan Cutpurse");
    }

    @Test
    @DisplayName("Every opponent chooses first, then those who didn't pay sacrifice together (CR 101.4)")
    void opponentsWhoDidNotPaySacrificeTogetherAfterTheLastAnswer() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player2, new FreshVolunteers());
        harness.addToBattlefield(player3, new FreshVolunteers());
        giveManaSource(player2);
        giveManaSource(player3);
        castRishadanCutpurse();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player3.getId());
        harness.handleMayAbilityChosen(player3, false);

        harness.assertInGraveyard(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player3, "Fresh Volunteers");
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }

    private void castRishadanCutpurse() {
        harness.castFromHand(player1, new RishadanCutpurse(), "{2}{U}");
    }
}
