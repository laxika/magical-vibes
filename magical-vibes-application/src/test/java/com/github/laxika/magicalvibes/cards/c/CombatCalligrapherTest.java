package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CombatCalligrapher.class, GrizzlyBears.class})
class CombatCalligrapherTest extends BaseCardTest {

    @Test
    @DisplayName("An attack creates one tapped and attacking flying Inkling for the attacking player")
    void attackCreatesInklingForAttackingPlayer() {
        addCreatureReady(player1, new CombatCalligrapher());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent inkling = findPermanents(player1, "Inkling").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(inkling.isTapped()).isTrue();
        assertThat(inkling.isAttacking()).isTrue();
        assertThat(inkling.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(inkling.getCard().getPower()).isEqualTo(2);
        assertThat(inkling.getCard().getToughness()).isEqualTo(1);
        assertThat(inkling.getCard().getColors()).containsExactlyInAnyOrder(
                com.github.laxika.magicalvibes.model.CardColor.WHITE,
                com.github.laxika.magicalvibes.model.CardColor.BLACK);
    }

    @Test
    @DisplayName("Inklings cannot attack the controller's player or planeswalker")
    void inklingsCannotAttackControllerOrPlaneswalker() {
        addCreatureReady(player1, new CombatCalligrapher());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, inklingCard());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0),
                Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An opponent attacking the controller does not create an Inkling")
    void opponentAttackDoesNotTrigger() {
        addCreatureReady(player1, new CombatCalligrapher());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player2, "Inkling")).isEmpty();
    }

    private Card inklingCard() {
        Card card = new Card();
        card.setName("Inkling");
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.INKLING));
        return card;
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
