package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GiantbaitingTest extends BaseCardTest {

    private List<Permanent> getTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Giant Warrior"))
                .toList();
    }

    @Test
    @DisplayName("Resolving creates one 4/4 Giant Warrior token with haste")
    void resolvingCreatesGiantWarriorToken() {
        harness.setHand(player1, List.of(new Giantbaiting()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        List<Permanent> tokens = getTokens();
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GIANT, CardSubtype.WARRIOR);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Created token is exiled at the beginning of the next end step")
    void tokenExiledAtEndStep() {
        harness.setHand(player1, List.of(new Giantbaiting()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(getTokens()).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isNotEmpty();

        // Advance to end step to trigger exile.
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, fires handleEndStepTriggers

        assertThat(getTokens()).isEmpty();
    }

    @Test
    @DisplayName("Conspire taps two color-sharing creatures and queues a copy of the spell")
    void conspireTapsCreaturesAndQueuesCopy() {
        harness.setHand(player1, List.of(new Giantbaiting()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent bearsA = addCreatureReady(player1, new GrizzlyBears()); // green
        Permanent bearsB = addCreatureReady(player1, new GrizzlyBears()); // green

        harness.castWithConspire(player1, 0, null, List.of(bearsA.getId(), bearsB.getId()));

        assertThat(bearsA.isTapped()).isTrue();
        assertThat(bearsB.isTapped()).isTrue();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack).anyMatch(e -> e.getEffectsToResolve().stream()
                .anyMatch(fx -> fx instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Conspire is rejected when a chosen creature does not share a color with the spell")
    void conspireRejectsColorlessCreature() {
        harness.setHand(player1, List.of(new Giantbaiting()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // green
        Permanent thopter = addCreatureReady(player1, new Ornithopter()); // colorless

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(bears.getId(), thopter.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
