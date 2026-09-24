package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EzioAuditoreDaFirenze.class, AssassinInitiate.class})
class EzioAuditoreDaFirenzeTest extends BaseCardTest {

    @Test
    @DisplayName("Assassin spells can use Ezio's granted freerunning cost after qualifying damage")
    void grantsFreerunningToAssassinSpells() {
        harness.addToBattlefield(player1, new EzioAuditoreDaFirenze());
        markAssassinCombatDamage();
        harness.setHand(player1, List.of(new AssassinInitiate()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Assassin Initiate");
    }

    @Test
    @DisplayName("Ezio's execution trigger fires only when the damaged player is at 10 or less life")
    void executesDamagedPlayerAtTenOrLessLife() {
        addAttackingEzio();
        harness.setLife(player2, 10);

        resolveCombatToMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Ezio does not offer execution when the damaged player is above 10 life")
    void doesNotExecuteDamagedPlayerAboveTenLife() {
        addAttackingEzio();
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Granted freerunning still requires qualifying combat damage")
    void grantedFreerunningRequiresCombatDamage() {
        harness.addToBattlefield(player1, new EzioAuditoreDaFirenze());
        harness.setHand(player1, List.of(new AssassinInitiate()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackingEzio() {
        Permanent ezio = addCreatureReady(player1, new EzioAuditoreDaFirenze());
        ezio.setAttacking(true);
        return ezio;
    }

    private void resolveCombatToMayPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void markAssassinCombatDamage() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
    }
}
