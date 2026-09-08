package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrickShot.class})
class TrickShotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to a creature and 2 damage to another creature token")
    void dealsDamageToCreatureAndCreatureToken() {
        Permanent creature = addCreature(player2, "Creature", 7, 7, false);
        Permanent token = addCreature(player2, "Soldier", 3, 3, true);
        castTrickShot(List.of(creature.getId(), token.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(6);
        assertThat(token.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can omit the optional creature token target")
    void canOmitCreatureTokenTarget() {
        Permanent creature = addCreature(player2, "Creature", 7, 7, false);
        castTrickShot(List.of(creature.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target a nontoken creature as the second target")
    void cannotTargetNontokenCreatureAsSecondTarget() {
        Permanent creature = addCreature(player2, "Creature", 7, 7, false);
        Permanent otherCreature = addCreature(player2, "Other Creature", 7, 7, false);

        harness.setHand(player1, List.of(new TrickShot()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), otherCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature token");
    }

    @Test
    @DisplayName("Cannot choose the same creature token for both targets")
    void cannotChooseSameCreatureTokenForBothTargets() {
        Permanent token = addCreature(player2, "Soldier", 3, 3, true);

        harness.setHand(player1, List.of(new TrickShot()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(token.getId(), token.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTrickShot(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new TrickShot()));
        addMana();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addCreature(Player player, String name,
                                  int power, int toughness, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(token);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
