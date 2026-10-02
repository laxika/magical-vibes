package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmeltingVat.class, WornPowerstone.class, Ornithopter.class, MindStone.class,
        GildedLotus.class, GrizzlyBears.class, Shock.class, Forest.class})
class SmeltingVatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another artifact and puts eligible artifacts within its mana value onto the battlefield")
    void sacrificesArtifactAndPutsEligibleCardsOntoBattlefield() {
        harness.addToBattlefield(player1, new SmeltingVat());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());

        Card ornithopter = new Ornithopter();
        Card firstMindStone = new MindStone();
        Card secondMindStone = new MindStone();
        Card gildedLotus = new GildedLotus();
        Card creature = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ornithopter, firstMindStone, secondMindStone,
                gildedLotus, creature, shock, forest, secondCreature));

        activateWithSacrifice(sacrificed);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                ornithopter.getId(), firstMindStone.getId(), secondMindStone.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.totalManaValueBound()).isEqualTo(3);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstMindStone.getId(), secondMindStone.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");

        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId(), firstMindStone.getId()));

        harness.assertInGraveyard(player1, "Worn Powerstone");
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == gildedLotus
                        || permanent.getCard() == creature
                        || permanent.getCard() == secondCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(secondMindStone, gildedLotus, creature, shock, forest, secondCreature);
    }

    @Test
    @DisplayName("Cannot sacrifice Smelting Vat itself")
    void requiresAnotherArtifact() {
        harness.addToBattlefield(player1, new SmeltingVat());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateWithSacrifice(Permanent sacrificed) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
    }
}
