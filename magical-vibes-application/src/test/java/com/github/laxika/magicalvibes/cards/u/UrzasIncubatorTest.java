package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrzasIncubatorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Urza's Incubator prompts for a creature type")
    void promptsForCreatureType() {
        harness.setHand(player1, List.of(new UrzasIncubator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GIANT");

        Permanent incubator = findPermanent(player1, "Urza's Incubator");
        assertThat(incubator.getChosenSubtype()).isEqualTo(CardSubtype.GIANT);
    }

    @Test
    @DisplayName("Creature spells of the chosen type cost {2} less")
    void reducesChosenCreatureTypeSpellCost() {
        addIncubator(CardSubtype.GIANT);
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    @DisplayName("Creature spells of another type are not reduced")
    void doesNotReduceAnotherCreatureType() {
        addIncubator(CardSubtype.GIANT);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not affect an opponent's creature spells")
    void doesNotReduceOpponentCreatureSpells() {
        addIncubator(CardSubtype.GIANT);
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addIncubator(CardSubtype chosenSubtype) {
        Permanent incubator = new Permanent(new UrzasIncubator());
        incubator.setChosenSubtype(chosenSubtype);
        gd.playerBattlefields.get(player1.getId()).add(incubator);
        return incubator;
    }
}
