package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VexingDevil.class})
class VexingDevilTest extends BaseCardTest {

    private void castAndResolveToChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new VexingDevil(), "{R}");
        harness.passBothPriorities(); // creature resolves → ETB trigger stacks
        harness.passBothPriorities(); // ETB resolves → opponent is offered the choice
    }

    @Test
    @DisplayName("Opponent declines — Devil stays, no damage")
    void decliningKeepsDevil() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Vexing Devil");
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Vexing Devil");
    }

    @Test
    @DisplayName("Opponent accepts — takes 4 damage and Devil is sacrificed")
    void acceptingDamagesAndSacrifices() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Vexing Devil");
        harness.assertInGraveyard(player1, "Vexing Devil");
    }

    @Test
    void acceptingStillSacrificesWhenAllDamageIsPrevented() {
        gd.playerDamagePreventionShields.put(player2.getId(), 4);
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Vexing Devil");
        harness.assertInGraveyard(player1, "Vexing Devil");
    }

    @Test
    void acceptingStillSacrificesWhenSomeDamageIsPrevented() {
        gd.playerDamagePreventionShields.put(player2.getId(), 2);
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Vexing Devil");
        harness.assertInGraveyard(player1, "Vexing Devil");
    }

    @Test
    void acceptingForSecondDevilDoesNotSacrificeFirstDevil() {
        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player2, false);
        var firstDevilId = harness.getPermanentId(player1, "Vexing Devil");

        castAndResolveToChoice();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getId())
                .containsExactly(firstDevilId);
        harness.assertInGraveyard(player1, "Vexing Devil");
    }

    @Test
    void triggerStillDealsDamageAfterDevilLeavesBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VexingDevil(), "{R}");
        harness.passBothPriorities();
        var devil = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, devil));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        harness.assertInHand(player1, "Vexing Devil");
        harness.assertNotInGraveyard(player1, "Vexing Devil");
        harness.assertNotOnBattlefield(player1, "Vexing Devil");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("The last opponent's decline asks for the Devil's death replacement before it is sacrificed")
    void lastDeclineAsksForTheDeathReplacementBeforeTheDevilIsSacrificed() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        castAndResolveToChoice();
        Permanent devil = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Vexing Devil"))
                .findFirst().orElseThrow();
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(devil.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.handleMayAbilityChosen(player3, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.rerunsEffect()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(devil);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player1.getId())).contains(devil.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(devil);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 16);
        harness.assertLife(player3, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
