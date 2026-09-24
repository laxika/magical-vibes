package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PromiseOfLoyalty.class, GrizzlyBears.class})
class PromiseOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player keeps one creature, puts a vow counter on it, and sacrifices the rest")
    void keepsOneCreaturePerPlayer() {
        Permanent ownKept = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownSacrificed = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingKept = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingSacrificed = addCreatureReady(player2, new GrizzlyBears());

        cast();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownKept.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingKept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownKept);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingKept);
        assertThat(ownKept.getCounterCount(CounterType.VOW)).isEqualTo(1);
        assertThat(opposingKept.getCounterCount(CounterType.VOW)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownSacrificed.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSacrificed.getCard());
    }

    @Test
    @DisplayName("A kept creature cannot attack the spell's controller")
    void keptCreatureCannotAttackController() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        cast();

        assertThat(opposingCreature.getCounterCount(CounterType.VOW)).isEqualTo(1);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A kept creature cannot attack the controller's planeswalker")
    void keptCreatureCannotAttackControllerPlaneswalker() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player1);

        cast();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0),
                Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opposingCreature.getCounterCount(CounterType.VOW)).isEqualTo(1);
    }

    private void cast() {
        harness.setHand(player1, List.of(new PromiseOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
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
