package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KamiOfAncientLaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CombatCalligrapher.class, KamiOfAncientLaw.class})
class CombatCalligrapherTest extends BaseCardTest {

    @Test
    @DisplayName("An attack creates one tapped and attacking flying Inkling for the attacking player")
    void attackCreatesInklingForAttackingPlayer() {
        addCreatureReady(player1, new CombatCalligrapher());
        addCreatureReady(player1, new KamiOfAncientLaw());

        declareAttackers(player1, List.of(1));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent inkling = findPermanents(player1, "Inkling").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
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
        addCreatureReady(player2, new KamiOfAncientLaw());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player2, "Inkling")).isEmpty();
    }

    @Test
    @DisplayName("Another player attacking an opponent creates an Inkling for that attacker")
    void anotherPlayerAttackingOpponentCreatesInkling() {
        Player opponent = addOpponent();
        addCreatureReady(player1, new CombatCalligrapher());
        addCreatureReady(player2, new CombatCalligrapher());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, opponent.getId()));

        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(findPermanents(player1, "Inkling")).isEmpty();
        assertThat(findPermanents(player2, "Inkling")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(opponent.getId());
                });
    }

    @Test
    @DisplayName("Each Calligrapher creates one Inkling when multiple creatures attack one player")
    void multipleAttackersCreateOneInkling() {
        addCreatureReady(player1, new CombatCalligrapher());
        addCreatureReady(player1, new CombatCalligrapher());
        // Each Calligrapher triggers once, regardless of the number of attackers.
        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        assertThat(findPermanents(player1, "Inkling")).hasSize(2);
    }

    @Test
    @DisplayName("Inklings cannot attack the controller directly")
    void inklingsCannotAttackController() {
        addCreatureReady(player1, new CombatCalligrapher());
        addCreatureReady(player2, inklingCard());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Attacking two different opponents creates an Inkling attacking each")
    void attackingDifferentOpponentsCreatesSeparateInklings() {
        Player opponent = addOpponent();
        addCreatureReady(player1, new CombatCalligrapher());
        addCreatureReady(player1, new KamiOfAncientLaw());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1),
                Map.of(0, player2.getId(), 1, opponent.getId()));

        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        assertThat(findPermanents(player1, "Inkling"))
                .extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(player2.getId(), opponent.getId());
    }

    @Test
    @DisplayName("Attacking only an opponent's planeswalker does not create an Inkling")
    void attackingPlaneswalkerDoesNotTrigger() {
        addCreatureReady(player1, new CombatCalligrapher());
        Permanent planeswalker = addPlaneswalker(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId())));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Inkling")).isEmpty();
    }
    private Player addOpponent() {
        Player opponent = new Player(UUID.randomUUID(), "Charlie");
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(opponent.getUsername());
        gd.playerIdToName.put(opponent.getId(), opponent.getUsername());
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
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
