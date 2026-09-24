package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaTheLastHope;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnakkeOathkeeper.class, GrizzlyBears.class, LilianaTheLastHope.class})
class OnakkeOathkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Taxes only creatures attacking a planeswalker it protects")
    void taxesOnlyPlaneswalkerAttackers() {
        harness.addToBattlefield(player1, new OnakkeOathkeeper());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0, 1), Map.of(0, planeswalker.getId(), 1, player1.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An attack at a protected planeswalker requires mana")
    void requiresManaToAttackPlaneswalker() {
        harness.addToBattlefield(player1, new OnakkeOathkeeper());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("The graveyard ability exiles Onakke Oathkeeper and returns a planeswalker")
    void returnsTargetPlaneswalkerFromGraveyard() {
        Card source = new OnakkeOathkeeper();
        Card planeswalker = new LilianaTheLastHope();
        harness.setGraveyard(player1, List.of(source, planeswalker));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(planeswalker.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(planeswalker.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(planeswalker);
    }

    @Test
    @DisplayName("The graveyard ability cannot target a creature card")
    void cannotTargetCreatureFromGraveyard() {
        Card source = new OnakkeOathkeeper();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(source, creature));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(4);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
