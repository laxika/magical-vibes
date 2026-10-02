package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonehewerGiant.class, ObsidianBattleAxe.class, StonybrookSchoolmaster.class})
class StonehewerGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for an Equipment, puts it onto the battlefield, and attaches to chosen creature")
    void searchesAndAttaches() {
        addCreatureReady(player1, new StonehewerGiant());
        Permanent creature = addCreatureReady(player1, new StonybrookSchoolmaster());
        harness.setLibrary(player1, List.of(new ObsidianBattleAxe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose the Equipment from the library search
        harness.handleCardChosen(player1, 0);
        // Choose the creature to attach it to
        harness.handlePermanentChosen(player1, creature.getId());

        Permanent equipment = findPermanent(player1, "Obsidian Battle-Axe");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can attach the found Equipment to the Giant itself")
    void attachesToGiantItself() {
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());
        harness.setLibrary(player1, List.of(new ObsidianBattleAxe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, giant.getId());

        Permanent equipment = findPermanent(player1, "Obsidian Battle-Axe");
        assertThat(equipment.getAttachedTo()).isEqualTo(giant.getId());
    }

    @Test
    @DisplayName("Finds no Equipment when the library has none")
    void noEquipmentInLibrary() {
        addCreatureReady(player1, new StonehewerGiant());
        harness.setLibrary(player1, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Obsidian Battle-Axe");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Requires {1}{W} to activate")
    void requiresManaToActivate() {
        addCreatureReady(player1, new StonehewerGiant());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate while the Giant is tapped")
    void cannotActivateWhenTapped() {
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());
        giant.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Offers only creatures the controller controls as attachment choices")
    void attachmentChoiceIsLimitedToControlledCreatures() {
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());
        Permanent ownCreature = addCreatureReady(player1, new StonybrookSchoolmaster());
        Permanent opponentCreature = addCreatureReady(player2, new StonybrookSchoolmaster());
        harness.setLibrary(player1, List.of(new ObsidianBattleAxe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds())
                .contains(giant.getId(), ownCreature.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(findPermanent(player1, "Obsidian Battle-Axe").getAttachedTo())
                .isEqualTo(ownCreature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leaves the found Equipment unattached if no creature remains")
    void leavesEquipmentUnattachedWhenNoCreatureRemains() {
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());
        harness.setLibrary(player1, List.of(new ObsidianBattleAxe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(giant);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Obsidian Battle-Axe").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
