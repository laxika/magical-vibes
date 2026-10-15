package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
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

@CardUsed({ChainToMemory.class, Forest.class, NyxbornColossus.class, NyxbornCourser.class})
class ChainToMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -4/-0 and scries 2")
    void weakensTargetCreatureAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 can put both cards on the bottom")
    void scryTwoPutsCardsOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.setLibrary(player1, List.of(new Forest(), new NyxbornColossus()));
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card firstTop = deck.get(0);
        Card secondTop = deck.get(1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(deck.get(deck.size() - 2)).isSameAs(firstTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(secondTop);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void doesNotScryWhenItsOnlyTargetLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        Card first = new Forest();
        Card second = new NyxbornColossus();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Chain to Memory");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetOwnCreatureAndReorderTopCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Card first = new Forest();
        Card second = new NyxbornColossus();
        Card third = new ChainToMemory();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        harness.assertInGraveyard(player1, "Chain to Memory");
    }

    @Test
    void canSplitScryCardsBetweenTopAndBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        Card first = new Forest();
        Card second = new NyxbornColossus();
        Card third = new ChainToMemory();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scriesTheOnlyCardInASingleCardLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillWeakensCreatureWithAnEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ChainToMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Chain to Memory");
        assertThat(gd.stack).isEmpty();
    }
}
