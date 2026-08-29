package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncubationIncongruityTest extends BaseCardTest {

    @Test
    @DisplayName("Incubation may reveal a creature from the top five and bottoms the rest randomly")
    void incubationRevealsCreatureAndBottomsRest() {
        LlanowarElves creature = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains = new Plains();
        Divination divination = new Divination();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(shock, creature, plains, divination, forest));
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Llanowar Elves", "Plains", "Divination", "Forest");
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Plains", "Divination", "Forest");
    }

    @Test
    @DisplayName("Incongruity exiles a creature and gives its controller a Frog Lizard")
    void incongruityExilesCreatureAndCreatesToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Frog Lizard"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.FROG, CardSubtype.LIZARD);
    }

    @Test
    @DisplayName("Incongruity cannot target a noncreature permanent")
    void incongruityCannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new IncubationIncongruity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
