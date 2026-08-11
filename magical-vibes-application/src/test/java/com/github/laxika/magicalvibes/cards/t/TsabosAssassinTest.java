package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TsabosAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature sharing the most common color and ignores regeneration")
    void destroysCreatureSharingMostCommonColor() {
        Permanent assassin = addAssassin();
        Permanent target = addCreature(player2, "Black Creature", CardColor.BLACK);
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Black Creature");
        harness.assertInGraveyard(player2, "Black Creature");
        assertThat(assassin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Destroys a creature when its color is tied for most common")
    void destroysCreatureWithTiedColor() {
        addAssassin();
        Permanent target = addCreature(player2, "Blue Creature", CardColor.BLUE);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Blue Creature");
        harness.assertInGraveyard(player2, "Blue Creature");
    }

    @Test
    @DisplayName("Allows any creature target but does nothing when the color condition is false")
    void doesNothingWhenTargetDoesNotShareMostCommonColor() {
        addAssassin();
        addCreature(player2, "Blue Creature 1", CardColor.BLUE);
        addCreature(player2, "Blue Creature 2", CardColor.BLUE);
        Permanent target = addCreature(player2, "White Creature", CardColor.WHITE);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "White Creature")).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addAssassin();
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAssassin() {
        harness.addToBattlefield(player1, new TsabosAssassin());
        Permanent assassin = findPermanent(player1, "Tsabo's Assassin");
        assassin.setSummoningSick(false);
        return assassin;
    }

    private Permanent addCreature(Player player, String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setColor(color);
        card.setColors(List.of(color));
        harness.addToBattlefield(player, card);
        return findPermanent(player, name);
    }
}
