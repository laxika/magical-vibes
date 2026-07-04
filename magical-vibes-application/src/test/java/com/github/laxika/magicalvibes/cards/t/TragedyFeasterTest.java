package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.DidntGainLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TragedyFeasterTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has Ward—Discard and an Infusion end-step 'sacrifice unless you gained life' downside")
    void hasCorrectStructure() {
        TragedyFeaster card = new TragedyFeaster();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL))
                .singleElement().isInstanceOf(CounterUnlessDiscardsEffect.class);

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED))
                .singleElement().isInstanceOf(ConditionalEffect.class);

        ConditionalEffect conditional =
                (ConditionalEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(conditional.condition()).isInstanceOf(DidntGainLifeThisTurn.class);
        assertThat(conditional.wrapped()).isInstanceOf(SacrificePermanentThenEffect.class);

        SacrificePermanentThenEffect sac = (SacrificePermanentThenEffect) conditional.wrapped();
        assertThat(sac.filter()).isInstanceOf(PermanentTruePredicate.class);
        assertThat(sac.thenEffect()).isNull(); // bare "sacrifice a permanent", no follow-up
    }

    // ===== Infusion: end-step sacrifice unless you gained life =====

    @Test
    @DisplayName("At end step with no life gained, controller must sacrifice a permanent")
    void sacrificesPermanentWhenNoLifeGained() {
        harness.addToBattlefield(player1, new TragedyFeaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // lifeGainedThisTurn left at 0

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve end-step trigger → begins sacrifice choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("No sacrifice happens when you gained life this turn")
    void noSacrificeWhenLifeGained() {
        harness.addToBattlefield(player1, new TragedyFeaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve end-step trigger → condition not met, does nothing

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Ward—Discard a card =====

    @Test
    @DisplayName("Ward triggers when an opponent targets Tragedy Feaster")
    void wardTriggersOnOpponentSpell() {
        Permanent feaster = new Permanent(new TragedyFeaster());
        feaster.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(feaster);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, feaster.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Tragedy Feaster");
    }

    // ===== Helpers =====

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN → END_STEP, triggers fire
    }
}
