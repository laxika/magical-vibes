package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WaterfallAerialistTest extends BaseCardTest {

    @Nested
    @DisplayName("Ward")
    class Ward {

        @Test
        @DisplayName("triggers when an opponent casts a spell targeting Waterfall Aerialist")
        void triggersWhenOpponentTargetsIt() {
            var aerialist = addReadyAerialist();
            castShock(aerialist.getId(), 1);

            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("counters the spell when its controller declines to pay {2}")
        void countersWhenOpponentDeclinesToPay() {
            var aerialist = addReadyAerialist();
            castShock(aerialist.getId(), 3);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player2, false);

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(card -> card instanceof Shock);
        }

        @Test
        @DisplayName("leaves the spell on the stack when its controller pays {2}")
        void spellRemainsWhenOpponentPays() {
            var aerialist = addReadyAerialist();
            castShock(aerialist.getId(), 3);
            harness.passBothPriorities();

            harness.handleMayAbilityChosen(player2, true);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Shock.class);
        }

        @Test
        @DisplayName("does not trigger when its controller casts a spell targeting it")
        void doesNotTriggerForControllerSpell() {
            var aerialist = addReadyAerialist();
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, aerialist.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Shock.class);
        }
    }

    private Permanent addReadyAerialist() {
        var aerialist = new Permanent(new WaterfallAerialist());
        aerialist.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(aerialist);
        return aerialist;
    }

    private void castShock(UUID targetId, int mana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, mana);
        harness.castInstant(player2, 0, targetId);
    }
}
