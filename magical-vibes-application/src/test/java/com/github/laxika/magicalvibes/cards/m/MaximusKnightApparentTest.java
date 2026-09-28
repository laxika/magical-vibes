package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MaximusKnightApparent.class, Spellbook.class})
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

    private Card equipment(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        card.setManaCost(manaCost);
        return card;
    }

    private void castMaximus() {
        harness.setHand(player1, List.of(new MaximusKnightApparent()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
