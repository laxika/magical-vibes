package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SurgeOfRighteousness.class)
class SurgeOfRighteousnessTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a red attacking creature and gains 2 life")
    void destroysRedAttackingCreatureAndGainsLife() {
        Permanent target = addCreature(player2, "Red Attacker", CardColor.RED, true, false);

        castAndResolve(target);

        harness.assertInGraveyard(player2, "Red Attacker");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Destroys a black blocking creature and gains 2 life")
    void destroysBlackBlockingCreatureAndGainsLife() {
        Permanent target = addCreature(player2, "Black Blocker", CardColor.BLACK, false, true);

        castAndResolve(target);

        harness.assertInGraveyard(player2, "Black Blocker");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetCreatureNotInCombat() {
        Permanent target = addCreature(player2, "Red Creature", CardColor.RED, false, false);
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Cannot target a nonblack, nonred attacking creature")
    void cannotTargetCreatureOfAnotherColor() {
        Permanent target = addCreature(player2, "Green Attacker", CardColor.GREEN, true, false);
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or red creature");
    }

    @Test
    @DisplayName("Does not gain life when the target leaves before resolution")
    void fizzlesIfTargetLeaves() {
        Permanent target = addCreature(player2, "Red Attacker", CardColor.RED, true, false);
        prepareSpell();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Surge of Righteousness");
    }

    private void castAndResolve(Permanent target) {
        prepareSpell();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SurgeOfRighteousness()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addCreature(Player player, String name, CardColor color,
                                   boolean attacking, boolean blocking) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(color);
        card.setColors(List.of(color));
        card.setPower(2);
        card.setToughness(2);

        Permanent permanent = new Permanent(card);
        permanent.setAttacking(attacking);
        permanent.setBlocking(blocking);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
