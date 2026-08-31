package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MagusOfTheMoat.class)
class MagusOfTheMoatTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures without flying cannot attack when Magus of the Moat is on the battlefield")
    void groundCreatureCannotAttack() {
        harness.addToBattlefield(player1, new MagusOfTheMoat());
        addReadyCreature(player2, creature(false));

        beginAttack(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Creatures with flying can attack when Magus of the Moat is on the battlefield")
    void flyingCreatureCanAttack() {
        harness.addToBattlefield(player1, new MagusOfTheMoat());
        addReadyCreature(player2, creature(true));

        beginAttack(player2);

        assertThatCode(() -> gs.declareAttackers(gd, player2, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The global restriction also affects the Magus controller's creatures")
    void controllersGroundCreatureCannotAttack() {
        harness.addToBattlefield(player1, new MagusOfTheMoat());
        addReadyCreature(player1, creature(false));

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card creature(boolean flying) {
        Card card = new Card();
        card.setName(flying ? "Test Flyer" : "Test Ground Creature");
        card.setType(CardType.CREATURE);
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
