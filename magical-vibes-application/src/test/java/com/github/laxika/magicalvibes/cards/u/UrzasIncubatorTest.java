package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.ElvishPiper;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzasIncubator.class, ElvishPiper.class, HulkingOgre.class})
class UrzasIncubatorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Urza's Incubator prompts for a creature type")
    void promptsForCreatureType() {
        harness.setHand(player1, List.of(new UrzasIncubator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "OGRE");

        Permanent incubator = findPermanent(player1, "Urza's Incubator");
        assertThat(incubator.getChosenSubtype()).isEqualTo(CardSubtype.OGRE);
    }

    @Test
    @DisplayName("Creature spells of the chosen type cost {2} less")
    void reducesChosenCreatureTypeSpellCost() {
        addIncubator(CardSubtype.OGRE);
        harness.setHand(player1, List.of(new HulkingOgre()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hulking Ogre");
    }

    @Test
    @DisplayName("Creature spells of another type are not reduced")
    void doesNotReduceAnotherCreatureType() {
        addIncubator(CardSubtype.OGRE);
        harness.setHand(player1, List.of(new ElvishPiper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not affect an opponent's creature spells")
    void doesNotReduceOpponentCreatureSpells() {
        addIncubator(CardSubtype.OGRE);
        harness.setHand(player2, List.of(new HulkingOgre()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addIncubator(CardSubtype chosenSubtype) {
        Permanent incubator = harness.addToBattlefieldAndReturn(player1, new UrzasIncubator());
        incubator.setChosenSubtype(chosenSubtype);
        return incubator;
    }
}
