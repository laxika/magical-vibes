package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrismariTheInspirationTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells get storm")
    void instantAndSorcerySpellsGetStorm() {
        harness.addToBattlefield(player1, new PrismariTheInspiration());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy))
                .singleElement()
                .extracting(entry -> entry.getCard().getName())
                .isEqualTo("Dark Ritual");
    }

    @Test
    @DisplayName("Creature spells do not get storm")
    void creatureSpellsDoNotGetStorm() {
        harness.addToBattlefield(player1, new PrismariTheInspiration());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    @DisplayName("Ward counters a spell unless its controller pays 5 life")
    void wardCountersWithoutPayment() {
        var prismari = harness.addToBattlefieldAndReturn(player1, new PrismariTheInspiration());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, prismari.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
