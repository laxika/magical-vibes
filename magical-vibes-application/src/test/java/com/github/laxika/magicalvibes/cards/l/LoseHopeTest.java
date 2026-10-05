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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WayfarersBauble());
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not scry when the only target leaves before resolution")
    void doesNotScryWithMissingTarget() {
        Permanent target = addCreatureReady(player2, new MoriokRigger());
        FillWithFright first = new FillWithFright();
        NightsWhisper second = new NightsWhisper();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Lose Hope");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can weaken your own creature with an empty library")
    void resolvesWithEmptyLibraryAndOwnCreature() {
        Permanent target = addCreatureReady(player1, new MoriokRigger());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lose Hope");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry 2 looks at the only card in a one-card library")
    void scriesWithOneCardLibrary() {
        Permanent target = addCreatureReady(player2, new MoriokRigger());
        NightsWhisper onlyCard = new NightsWhisper();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        harness.assertInGraveyard(player1, "Lose Hope");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scry can put both cards on the bottom in either order")
    void putsBothCardsOnBottomInChosenOrder() {
        Permanent target = addCreatureReady(player2, new MoriokRigger());
        FillWithFright first = new FillWithFright();
        NightsWhisper second = new NightsWhisper();
        WayfarersBauble third = new WayfarersBauble();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
        harness.assertInGraveyard(player1, "Lose Hope");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Scry can keep both cards on top in either order")
    void keepsBothCardsOnTopInChosenOrder() {
        Permanent target = addCreatureReady(player2, new MoriokRigger());
        FillWithFright first = new FillWithFright();
        NightsWhisper second = new NightsWhisper();
        WayfarersBauble third = new WayfarersBauble();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new LoseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        harness.assertInGraveyard(player1, "Lose Hope");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
