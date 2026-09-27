package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChampionsOfMinasTirith.class, GrizzlyBears.class})
class ChampionsOfMinasTirithTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller becomes the monarch when it enters")
    void becomesMonarchWhenItEnters() {
        addChampionsOfMinasTirith();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The active opponent may pay their hand size to attack the monarch")
    void payingHandSizeAllowsAttacks() {
        addChampionsOfMinasTirith();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        resolveCombatTrigger(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("Declining to pay prevents attacks for this combat")
    void decliningToPayPreventsAttacksForThisCombat() {
        addChampionsOfMinasTirith();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatTrigger(player2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, false));

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        gd.expireEndOfCombatFloatingEffects();
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("Having less mana than the hand size also prevents attacks")
    void insufficientManaPreventsAttacks() {
        addChampionsOfMinasTirith();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        resolveCombatTrigger(player2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, true));

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The ability does not trigger when its controller is not the monarch")
    void doesNotTriggerWhenControllerIsNotMonarch() {
        addChampionsOfMinasTirith();
        gd.monarchPlayerId = player2.getId();
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombatTrigger(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        declareAttackers(player2, List.of(0));
    }

    private void resolveCombatTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void addChampionsOfMinasTirith() {
        harness.enterBattlefieldAndReturn(player1, new ChampionsOfMinasTirith());
        resolveAllTriggers();
    }
}
