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
    @DisplayName("The reduction also affects an opponent's creature spells")
    void reducesOpponentCreatureSpells() {
        addIncubator(CardSubtype.OGRE);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HulkingOgre()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.assertNotInHand(player2, "Hulking Ogre");
    }

    @Test
    @DisplayName("Multiple Incubators combine their reductions")
    void multipleIncubatorsReduceGenericCostToZero() {
        addIncubator(CardSubtype.ELF);
        addIncubator(CardSubtype.ELF);
        harness.setHand(player1, List.of(new ElvishPiper()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Elvish Piper");
    }

    @Test
    @DisplayName("Even multiple Incubators do not pay colored mana requirements")
    void doesNotReduceColoredMana() {
        addIncubator(CardSubtype.OGRE);
        addIncubator(CardSubtype.OGRE);
        harness.setHand(player1, List.of(new HulkingOgre()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Hulking Ogre");
    }

    @Test
    @DisplayName("The chosen type matches any of a creature spell's creature types")
    void reducesCreatureWithMatchingSecondarySubtype() {
        addIncubator(CardSubtype.SHAMAN);
        harness.setHand(player1, List.of(new ElvishPiper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Elvish Piper");
    }

    @Test
    @DisplayName("A tapped Incubator still reduces creature spell costs")
    void tappedIncubatorStillReducesCosts() {
        addIncubator(CardSubtype.OGRE).tap();
        harness.setHand(player1, List.of(new HulkingOgre()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Hulking Ogre");
    }

    @Test
    @DisplayName("Incubator does not reduce the cost of another Incubator")
    void doesNotReduceNoncreatureSpellCost() {
        addIncubator(CardSubtype.OGRE);
        harness.setHand(player1, List.of(new UrzasIncubator()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Urza's Incubator");
    }

    private Permanent addIncubator(CardSubtype chosenSubtype) {
        Permanent incubator = harness.addToBattlefieldAndReturn(player1, new UrzasIncubator());
        incubator.setChosenSubtype(chosenSubtype);
        return incubator;
    }
}
