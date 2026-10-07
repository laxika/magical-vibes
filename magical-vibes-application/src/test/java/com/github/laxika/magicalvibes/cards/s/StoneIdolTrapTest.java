package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GnarlidPack;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ExilePermanentAtControllerEndStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneIdolTrap.class, GnarlidPack.class})
class StoneIdolTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1} less for each attacking creature controlled by the opponent")
    void costIsReducedForEachAttackingCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent attacker1 = addCreatureReady(player2, new GnarlidPack());
        Permanent attacker2 = addCreatureReady(player2, new GnarlidPack());
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);

        harness.setHand(player1, List.of(new StoneIdolTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Nonattacking creatures do not reduce the cost")
    void nonattackingCreaturesDoNotReduceCost() {
        addCreatureReady(player1, new GnarlidPack());
        addCreatureReady(player2, new GnarlidPack());

        harness.setHand(player1, List.of(new StoneIdolTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creates a trampling Construct token and exiles it at the controller's next end step")
    void createsAndExilesConstructAtControllersNextEndStep() {
        harness.setHand(player1, List.of(new StoneIdolTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(12);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
        assertThat(token.getCard().getKeywords()).containsExactly(Keyword.TRAMPLE);
        assertThat(gd.getDelayedActions(ExilePermanentAtControllerEndStep.class))
                .contains(new ExilePermanentAtControllerEndStep(token.getId(), player1.getId()));

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        gd.interaction.clearAwaitingInput();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Six attackers reduce only the generic portion of the cost")
    void excessReductionStillRequiresRedMana() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        for (int i = 0; i < 6; i++) {
            addCreatureReady(player2, new GnarlidPack()).setAttacking(true);
        }
        harness.setHand(player1, List.of(new StoneIdolTrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting during your end step waits for your next turn's end step")
    void castingDuringEndStepWaitsForNextOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new StoneIdolTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player1, 0);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Each separately resolved spell creates a token with its own delayed exile")
    void separatelyResolvedSpellsScheduleIndependentExiles() {
        harness.setHand(player1, List.of(new StoneIdolTrap(), new StoneIdolTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);
        List<Permanent> tokens = List.copyOf(gd.playerBattlefields.get(player1.getId()));
        assertThat(tokens).hasSize(2);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrderElementsOf(tokens);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Your own attacking creatures also reduce the cost")
    void ownAttackersReduceCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        addCreatureReady(player1, new GnarlidPack()).setAttacking(true);
        harness.setHand(player1, List.of(new StoneIdolTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A token created after attackers are declared can block immediately")
    void newlyCreatedConstructCanBlock() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        Permanent attacker = addCreatureReady(player2, new GnarlidPack());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.setHand(player1, List.of(new StoneIdolTrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.passUntil(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }
}
