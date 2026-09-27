package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicCrucible.class, Divination.class, Forest.class, GrizzlyBears.class})
class CosmicCrucibleTest extends BaseCardTest {

    @Test
    @DisplayName("Adds four mana in individually chosen colors during the controller's first main phase")
    void addsFourManaAtBeginningOfFirstMainPhase() {
        harness.addToBattlefield(player1, new CosmicCrucible());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May copy one noncreature spell each turn")
    void mayCopyOnlyOneNoncreatureSpellEachTurn() {
        harness.addToBattlefield(player1, new CosmicCrucible());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Does not trigger for creature spells")
    void doesNotCopyCreatureSpells() {
        harness.addToBattlefield(player1, new CosmicCrucible());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Copies of permanent spells become tokens")
    void copiesPermanentSpellsAsTokens() {
        harness.addToBattlefield(player1, new CosmicCrucible());
        harness.setHand(player1, List.of(new CosmicCrucible()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cosmic Crucible")).hasSize(3);
        assertThat(findPermanents(player1, "Cosmic Crucible")).filteredOn(
                permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
