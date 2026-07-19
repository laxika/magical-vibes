package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DamageAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuenchableFireTest extends BaseCardTest {

    // "Quenchable Fire deals 3 damage to target player or planeswalker. It deals an additional
    //  3 damage to that player or planeswalker at the beginning of your next upkeep step unless
    //  that player or that planeswalker's controller pays {U} before that step."

    private void giveQuenchableFire() {
        harness.setHand(player1, List.of(new QuenchableFire()));
        harness.addMana(player1, ManaColor.RED, 4);
    }

    /** Simulate the beginning of player1's (the caster's) next upkeep, surfacing the pay-or-damage prompt. */
    private void reachUpkeepPrompt() {
        gd.turnNumber = 3; // skip the turn-1 opening-hand handling
        gd.activePlayerId = player1.getId();
        GameTestEngineContext.get().getBean(StepTriggerService.class).handleUpkeepTriggers(gd);
        harness.passBothPriorities(); // resolve the pushed trigger -> pay-or-damage may prompt
    }

    @Test
    @DisplayName("Deals 3 damage now and schedules the delayed obligation against the target")
    void immediateDamageAndScheduledObligation() {
        giveQuenchableFire();
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);

        List<DamageAtNextUpkeepUnlessPays> scheduled = gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().spellControllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().targetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining to pay {U} deals the additional 3 damage at the caster's next upkeep")
    void declineDealsAdditionalDamage() {
        giveQuenchableFire();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        int lifeAfterImmediate = gd.getLife(player2.getId());

        reachUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterImmediate - 3);
        assertThat(gd.getDelayedActions(DamageAtNextUpkeepUnlessPays.class)).isEmpty();
    }

    @Test
    @DisplayName("Paying {U} avoids the additional damage")
    void payAvoidsAdditionalDamage() {
        giveQuenchableFire();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        int lifeAfterImmediate = gd.getLife(player2.getId());

        reachUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player2, ManaColor.BLUE, 1); // mana empties between steps — add it at payment time
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterImmediate);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Targeting a planeswalker: its controller may pay {U}, else it loses 3 more loyalty")
    void planeswalkerControllerDeclinesLosesLoyalty() {
        ElspethKnightErrant elspethCard = new ElspethKnightErrant();
        Permanent elspeth = new Permanent(elspethCard);
        elspeth.setCounterCount(CounterType.LOYALTY, 8);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);
        giveQuenchableFire();

        harness.castSorcery(player1, 0, elspeth.getId());
        harness.passBothPriorities();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 8 - 3

        reachUpkeepPrompt();

        // The planeswalker's controller (player2), not the caster, is asked to pay.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 5 - 3
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        giveQuenchableFire();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }
}
