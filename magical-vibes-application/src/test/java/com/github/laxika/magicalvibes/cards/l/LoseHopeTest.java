package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FillWithFright;
import com.github.laxika.magicalvibes.cards.m.MoriokRigger;
import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoseHope.class, FillWithFright.class, MoriokRigger.class, NightsWhisper.class,
        WayfarersBauble.class})
class LoseHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -1/-1 and scries 2")
    void weakensTargetCreatureAndScries() {
        Permanent target = addCreatureReady(player2, new MoriokRigger());
        harness.setLibrary(player1, List.of(new FillWithFright(), new NightsWhisper()));
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry reorder finishes resolving")
    void scryReordersLibraryAndFinishesResolving() {
        Permanent target = addCreatureReady(player2, new MoriokRigger());
        FillWithFright bottom = new FillWithFright();
        NightsWhisper top = new NightsWhisper();
        harness.setLibrary(player1, List.of(bottom, top));
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lose Hope");
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new MoriokRigger());
        harness.setLibrary(player1, List.of(new FillWithFright(), new NightsWhisper()));
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WayfarersBauble());
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
