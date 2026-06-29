package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.UntapEachOtherCreatureYouControlEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CopperhornScoutTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_ATTACK trigger to untap other creatures")
    void hasCorrectEffect() {
        CopperhornScout card = new CopperhornScout();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(UntapEachOtherCreatureYouControlEffect.class);
    }

    // ===== Attack trigger fires =====

    @Test
    @DisplayName("Attacking puts trigger on the stack")
    void attackPutsTriggerOnStack() {
        Permanent scout = addCreatureReady(player1, new CopperhornScout());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Copperhorn Scout");
    }

    // ===== Untap behavior =====

    @Test
    @DisplayName("Resolving trigger untaps each other tapped creature you control")
    void untapsOtherTappedCreatures() {
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent bear1 = addCreatureReady(player1, new GrizzlyBears());
        bear1.tap();
        Permanent bear2 = addCreatureReady(player1, new GrizzlyBears());
        bear2.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap the Scout itself")
    void doesNotUntapSelf() {
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        // Scout attacked so it's tapped — should NOT be untapped by its own trigger
        assertThat(scout.isTapped()).isTrue();
        // Bear should be untapped
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        opponentBear.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not affect already-untapped creatures")
    void doesNotAffectAlreadyUntappedCreatures() {
        Permanent scout = addCreatureReady(player1, new CopperhornScout());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        // bear is not tapped

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(bear.isTapped()).isFalse();
    }

    // ===== Helper methods =====


    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
