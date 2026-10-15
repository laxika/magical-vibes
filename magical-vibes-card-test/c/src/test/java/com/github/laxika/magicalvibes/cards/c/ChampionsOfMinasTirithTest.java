package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
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

@CardUsed({ChampionsOfMinasTirith.class, ElvishMystic.class})
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
        addCreatureReady(player2, new ElvishMystic());
        harness.setHand(player2, List.of(new ElvishMystic(), new ElvishMystic()));
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
        addCreatureReady(player2, new ElvishMystic());
        harness.setHand(player2, List.of(new ElvishMystic(), new ElvishMystic()));

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
        addCreatureReady(player2, new ElvishMystic());
        harness.setHand(player2, List.of(new ElvishMystic(), new ElvishMystic()));

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
        addCreatureReady(player2, new ElvishMystic());

        resolveCombatTrigger(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("An opponent with an empty hand can pay zero to attack")
    void emptyHandCanPayZero() {
        addChampionsOfMinasTirith();
        addCreatureReady(player2, new ElvishMystic());
        harness.setHand(player2, List.of());

        resolveCombatTrigger(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, true));

        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("An opponent can decline even a zero mana payment and cannot attack")
    void decliningZeroStillPreventsAttacks() {
        addChampionsOfMinasTirith();
        addCreatureReady(player2, new ElvishMystic());
        harness.setHand(player2, List.of());

        resolveCombatTrigger(player2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, false));

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Losing the monarchy before resolution prevents the payment and restriction")
    void losingMonarchyBeforeResolutionDoesNothing() {
        addChampionsOfMinasTirith();
        addCreatureReady(player2, new ElvishMystic());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        gd.monarchPlayerId = player2.getId();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("The payment uses the opponent's hand size at resolution")
    void handSizeIsEvaluatedAtResolution() {
        addChampionsOfMinasTirith();
        addCreatureReady(player2, new ElvishMystic());
        harness.setHand(player1, List.of(new ElvishMystic(), new ElvishMystic(), new ElvishMystic()));
        harness.setHand(player2, List.of(new ElvishMystic(), new ElvishMystic()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        harness.setHand(player2, List.of(new ElvishMystic()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, true));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("The ability does not trigger on its controller's turn")
    void doesNotTriggerOnControllersTurn() {
        addChampionsOfMinasTirith();

        resolveCombatTrigger(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Champions in response does not stop its attack restriction")
    void restrictionResolvesAfterSourceLeavesBattlefield() {
        addChampionsOfMinasTirith();
        addCreatureReady(player2, new ElvishMystic());
        harness.setHand(player2, List.of(new ElvishMystic()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        var champions = findPermanent(player1, "Champions of Minas Tirith");
        gd.playerBattlefields.get(player1.getId()).remove(champions);
        gd.playerGraveyards.get(player1.getId()).add(champions.getCard());
        resolveAllTriggers();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleMayAbilityChosen(player2, false));

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
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
