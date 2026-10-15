package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.d.DaringSaboteur;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PirateHat.class, DaringSaboteur.class, Forest.class, GrizzlyBears.class, ArmoredKincaller.class})
class PirateHatTest extends BaseCardTest {

    @Test
    @DisplayName("Equip Pirate attaches Pirate Hat and boosts the Pirate")
    void pirateEquipAttachesAndBoostsPirate() {
        Permanent hat = addHatReady(player1);
        Permanent pirate = addCreatureReady(player1, new DaringSaboteur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, pirate.getId());
        harness.passBothPriorities();

        assertThat(hat.getAttachedTo()).isEqualTo(pirate.getId());
        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(2);
    }

    @Test
    @DisplayName("Generic equip attaches Pirate Hat to a non-Pirate creature")
    void genericEquipAttachesToNonPirate() {
        Permanent hat = addHatReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hat.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip Pirate rejects a non-Pirate creature")
    void pirateEquipRejectsNonPirate() {
        addHatReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pirate creature");
    }

    @Test
    @DisplayName("Attacking with the equipped creature draws then discards")
    void attackTriggersLoot() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        Permanent hat = addHatReady(player1);
        Permanent pirate = addCreatureReady(player1, new DaringSaboteur());
        hat.setAttachedTo(pirate.getId());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A non-Pirate attacker may discard the card it just drew")
    void nonPirateAttackerCanDiscardDrawnCard() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new ArmoredKincaller()));
        Permanent hat = addHatReady(player1);
        Permanent creature = addCreatureReady(player1, new ArmoredKincaller());
        hat.setAttachedTo(creature.getId());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Armored Kincaller");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Armored Kincaller");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The equipped creature's controller loots even when the opponent controls the Hat")
    void creatureControllerReceivesLoot() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new ArmoredKincaller()));
        harness.setLibrary(player2, List.of(new ArmoredKincaller()));
        Permanent hat = addHatReady(player1);
        Permanent creature = addCreatureReady(player2, new ArmoredKincaller());
        hat.setAttachedTo(creature.getId());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInHand(player2, "Armored Kincaller");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Removing Pirate Hat after the attack does not remove the loot trigger")
    void lootTriggerSurvivesHatLeavingBattlefield() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new ArmoredKincaller()));
        Permanent hat = addHatReady(player1);
        Permanent creature = addCreatureReady(player1, new ArmoredKincaller());
        hat.setAttachedTo(creature.getId());

        declareAttackers(List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(hat);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Armored Kincaller");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Re-equipping moves both the bonus and the attack ability")
    void reEquipMovesBonusAndLootAbility() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new ArmoredKincaller()));
        Permanent hat = addHatReady(player1);
        Permanent formerWearer = addCreatureReady(player1, new ArmoredKincaller());
        Permanent newWearer = addCreatureReady(player1, new ArmoredKincaller());
        hat.setAttachedTo(formerWearer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, newWearer.getId());
        harness.passBothPriorities();

        assertThat(hat.getAttachedTo()).isEqualTo(newWearer.getId());
        assertThat(gqs.getEffectivePower(gd, formerWearer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, formerWearer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, newWearer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, newWearer)).isEqualTo(4);

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Armored Kincaller");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Both equip abilities reject opposing creatures")
    void equipRejectsOpposingCreatures() {
        addHatReady(player1);
        Permanent opponentPirate = addCreatureReady(player2, new DaringSaboteur());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentPirate.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentPirate.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addHatReady(Player player) {
        return addCreatureReady(player, new PirateHat());
    }
}
