package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeferisMoatTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Teferi's Moat asks its controller to choose a color")
    void resolvingAsksForColor() {
        harness.setHand(player1, List.of(new TeferisMoat()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("A chosen-color creature without flying cannot attack the Moat controller")
    void chosenColorGroundCreatureCannotAttackController() {
        addMoatWithChosenColor(player2, CardColor.RED);
        addCreatureReady(player1, creature("Red Creature", CardColor.RED, false));

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack");
    }

    @Test
    @DisplayName("A flying creature of the chosen color can attack the Moat controller")
    void chosenColorFlyerCanAttackController() {
        addMoatWithChosenColor(player2, CardColor.RED);
        addCreatureReady(player1, creature("Red Flyer", CardColor.RED, true));

        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("A creature of another color can attack the Moat controller")
    void differentColorCreatureCanAttackController() {
        addMoatWithChosenColor(player2, CardColor.RED);
        addCreatureReady(player1, creature("Green Creature", CardColor.GREEN, false));

        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(0));
    }

    private void addMoatWithChosenColor(Player controller, CardColor color) {
        Permanent moat = new Permanent(new TeferisMoat());
        moat.setChosenColor(color);
        gd.playerBattlefields.get(controller.getId()).add(moat);
    }

    private static Card creature(String name, CardColor color, boolean flying) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        if (flying) {
            card.setKeywords(EnumSet.of(Keyword.FLYING));
        }
        return card;
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
