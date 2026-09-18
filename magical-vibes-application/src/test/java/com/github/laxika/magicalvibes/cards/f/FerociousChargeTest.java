package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerociousCharge.class, HillGiant.class, GrizzlyBears.class, Forest.class, FountainOfYouth.class})
class FerociousChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts target creature by +4/+4 and scries 2")
    void boostsTargetCreatureAndScries() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new FerociousCharge()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(7);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry reorder finishes resolving")
    void scryReordersLibraryAndFinishesResolving() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        Card bottom = new Forest();
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bottom, top));
        harness.setHand(player1, List.of(new FerociousCharge()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ferocious Charge");
    }

    @Test
    @DisplayName("Boost wears off at the cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FerociousCharge()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not scry when the target leaves before resolution")
    void doesNotResolveIfTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new FerociousCharge()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Ferocious Charge");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new FerociousCharge()));
        addMana();

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
