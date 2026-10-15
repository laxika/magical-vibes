package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloakAndDagger;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.cards.y.YavimayaScion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StonehewerGiant.class, ObsidianBattleAxe.class, StonybrookSchoolmaster.class, YavimayaScion.class, CloakAndDagger.class})
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

    @Test
    @DisplayName("May fail to find even when the library contains Equipment")
    void mayFailToFindEquipment() {
        addCreatureReady(player1, new StonehewerGiant());
        ObsidianBattleAxe equipmentCard = new ObsidianBattleAxe();
        harness.setLibrary(player1, List.of(equipmentCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipmentCard);
        harness.assertNotOnBattlefield(player1, "Obsidian Battle-Axe");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new StonehewerGiant());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("The ability still searches and attaches after the Giant leaves")
    void resolvesAfterGiantLeaves() {
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());
        Permanent creature = addCreatureReady(player1, new StonybrookSchoolmaster());
        harness.setLibrary(player1, List.of(new ObsidianBattleAxe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(giant);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Obsidian Battle-Axe").getAttachedTo())
                .isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Protection from artifacts excludes a creature from the mandatory attachment choice")
    void excludesCreaturesThatCannotBeEquipped() {
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());
        Permanent protectedCreature = addCreatureReady(player1, new YavimayaScion());
        harness.setLibrary(player1, List.of(new ObsidianBattleAxe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        List<UUID> offeredCreatures = List.copyOf(choice.validPermanentIds());
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(findPermanent(player1, "Obsidian Battle-Axe").getAttachedTo())
                .isEqualTo(giant.getId());
        assertThat(offeredCreatures).contains(giant.getId()).doesNotContain(protectedCreature.getId());
    }

    @Test
    @DisplayName("Attaches to a creature with shroud because the ability does not target")
    void attachesToCreatureWithShroud() {
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakAndDagger());
        cloak.setAttachedTo(giant.getId());
        harness.setLibrary(player1, List.of(new ObsidianBattleAxe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(findPermanent(player1, "Obsidian Battle-Axe").getAttachedTo())
                .isEqualTo(giant.getId());
        assertThat(cloak.getAttachedTo()).isEqualTo(giant.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Resolves without asking for a card when the library is empty")
    void resolvesWithEmptyLibrary() {
        Permanent giant = addCreatureReady(player1, new StonehewerGiant());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(giant.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Obsidian Battle-Axe");
    }
}
