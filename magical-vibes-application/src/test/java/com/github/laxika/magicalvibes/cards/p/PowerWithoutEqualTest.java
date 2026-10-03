package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerWithoutEqual.class, Forest.class, GrizzlyBears.class})
class PowerWithoutEqualTest extends BaseCardTest {

    @Test
    void drawsThreeAndGrantsNoMaximumHandSizeUntilNextTurn() {
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, drawn);

        resolveScheme();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
        assertThat(gd.playersWithNoMaximumHandSizeUntilNextTurn).contains(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void castsUpToThreeSpellsWhenControllerHasSixLands() {
        addLands(6);
        List<Card> spells = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(spells));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        resolveScheme();

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                    .isNotNull();
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack)
                .filteredOn(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL)
                .extracting(StackEntry::getCard)
                .containsExactlyInAnyOrder(spells.get(0), spells.get(1), spells.get(2));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spells.get(3));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotOfferFreeSpellsWithFewerThanSixLands() {
        addLands(5);
        Card spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        resolveScheme();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
    }

    private void resolveScheme() {
        PowerWithoutEqual scheme = new PowerWithoutEqual();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }
}
