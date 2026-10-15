package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterthornNissasAnimus.class, GrizzlyBears.class, Forest.class})
class BitterthornNissasAnimusTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates and equips a Phyrexian Germ")
    void livingWeaponCreatesAndEquipsGerm() {
        harness.castFromHand(player1, new BitterthornNissasAnimus(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent equipment = findPermanent(player1, "Bitterthorn, Nissa's Animus");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(equipment.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with the equipped creature may search for a tapped basic land")
    void attackMaySearchForBasicLand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BitterthornNissasAnimus());
        equipment.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void equipMovesBonusAndUnsupportedGermDies() {
        harness.castFromHand(player1, new BitterthornNissasAnimus(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = findPermanent(player1, "Bitterthorn, Nissa's Animus");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(germ);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void optionalSearchCanBeDeclined() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BitterthornNissasAnimus());
        equipment.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void equipmentControllerSearchesWhenOpponentsCreatureAttacks() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BitterthornNissasAnimus());
        equipment.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        GrizzlyBears opponentLibraryCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentLibraryCard));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
    }

    @Test
    void attackingWithUnequippedCreatureDoesNotTriggerSearch() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BitterthornNissasAnimus());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchMayFailToFindEvenWhenBasicLandExists() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BitterthornNissasAnimus());
        equipment.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchResolvesAfterEquipmentBecomesUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BitterthornNissasAnimus());
        equipment.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        equipment.setAttachedTo(null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void searchWithNoBasicLandFinishesWithoutPuttingCardOntoBattlefield() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BitterthornNissasAnimus());
        equipment.setAttachedTo(creature.getId());
        GrizzlyBears libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
