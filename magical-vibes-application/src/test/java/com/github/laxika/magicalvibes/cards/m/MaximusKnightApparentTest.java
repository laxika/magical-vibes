package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Batterskull;
import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.s.Shadowspear;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({MaximusKnightApparent.class, Spellbook.class, LightningGreaves.class,
        Shadowspear.class, Batterskull.class})
class MaximusKnightApparentTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield may search for an Equipment with mana value 2")
    void enteringMaySearchForEquipmentWithManaValueTwo() {
        Card tooExpensive = equipment("Too Expensive Equipment", "{4}");
        Card eligible = equipment("Eligible Equipment", "{2}");
        harness.setLibrary(player1, List.of(tooExpensive, eligible));
        castMaximus();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(eligible);
    }

    @Test
    @DisplayName("Declining the search leaves the library unchanged")
    void decliningSearchLeavesLibraryUnchanged() {
        Card equipment = equipment("Eligible Equipment", "{2}");
        harness.setLibrary(player1, List.of(equipment));
        castMaximus();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
    }

    @Test
    @DisplayName("Sacrificing an artifact grants two energy counters")
    void sacrificingArtifactGrantsTwoEnergyCounters() {
        harness.addToBattlefield(player1, new MaximusKnightApparent());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("The search excludes Equipment with mana value below or above two")
    void searchRequiresExactlyManaValueTwo() {
        Card cheap = new Shadowspear();
        Card eligible = new LightningGreaves();
        Card expensive = new Batterskull();
        harness.setLibrary(player1, List.of(cheap, eligible, expensive));
        castMaximus();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(eligible);
    }

    @Test
    @DisplayName("The selected Equipment moves from the library to its controller's hand")
    void selectedEquipmentGoesToHand() {
        Card eligible = new LightningGreaves();
        Card other = new Batterskull();
        harness.setLibrary(player1, List.of(eligible, other));
        castMaximus();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        harness.assertNotOnBattlefield(player1, "Lightning Greaves");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller can fail to find even when an eligible Equipment is present")
    void mayFailToFindEligibleEquipment() {
        Card eligible = new LightningGreaves();
        harness.setLibrary(player1, List.of(eligible));
        castMaximus();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrifice is paid on activation and energy is received only on resolution")
    void sacrificeIsPaidBeforeEnergyResolves() {
        Permanent maximus = harness.addToBattlefieldAndReturn(player1, new MaximusKnightApparent());
        maximus.setTapped(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LightningGreaves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player1, "Lightning Greaves");
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new MaximusKnightApparent());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LightningGreaves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @CardUsed({LiquimetalCoating.class})
    @DisplayName("Maximus can sacrifice himself when he has become an artifact")
    void canSacrificeSelfWhenArtifact() {
        Permanent maximus = harness.addToBattlefieldAndReturn(player1, new MaximusKnightApparent());
        harness.addToBattlefield(player2, new LiquimetalCoating());
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, maximus.getId());
        harness.passBothPriorities();
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Maximus, Knight Apparent");
        harness.assertNotOnBattlefield(player1, "Maximus, Knight Apparent");
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    private Card equipment(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        card.setManaCost(manaCost);
        return card;
    }

    private void castMaximus() {
        harness.castFromHand(player1, new MaximusKnightApparent(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
