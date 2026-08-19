package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmphibiousKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Amphibious Kavu gets +3/+3 when blocked by a blue creature")
    void becomesBlockedByBlueCreatureBoosts() {
        Permanent kavu = addKavu(player1);
        kavu.setAttacking(true);
        addReadyCreature(player2, CardColor.BLUE);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Amphibious Kavu gets +3/+3 when blocking a black creature")
    void blocksBlackCreatureBoosts() {
        addReadyCreature(player1, CardColor.BLACK).setAttacking(true);
        Permanent kavu = addKavu(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Amphibious Kavu does not get a boost from a creature of another color")
    void doesNotBoostForOtherColor() {
        Permanent kavu = addKavu(player1);
        kavu.setAttacking(true);
        addReadyCreature(player2, CardColor.RED);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isZero();
        assertThat(kavu.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Amphibious Kavu gets only one boost when blocked by multiple matching creatures")
    void multipleMatchingBlockersBoostOnce() {
        Permanent kavu = addKavu(player1);
        kavu.setAttacking(true);
        addReadyCreature(player2, CardColor.BLUE);
        addReadyCreature(player2, CardColor.BLACK);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);
    }

    private Permanent addKavu(Player player) {
        return addCreatureReady(player, new AmphibiousKavu());
    }

    private Permanent addReadyCreature(Player player, CardColor color) {
        return addCreatureReady(player, createCreature(color));
    }

    private static Card createCreature(CardColor color) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setColor(color);
        card.setColors(List.of(color));
        return card;
    }
}
