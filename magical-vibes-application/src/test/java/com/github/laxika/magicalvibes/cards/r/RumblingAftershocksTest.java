package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ApexHawks;
import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.t.TasteOfParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RumblingAftershocksTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToTheNumberOfMultikickerPayments() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castCreatureWithMultikicker(List.of("{1}{W}", "{1}{W}"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void dealsOneDamageForARegularKickedSpell() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castKickedInstant(player1, 0, player2.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    void doesNotTriggerForAnUnrelatedRepeatableCost() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.setHand(player1, List.of(new TasteOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{1}{G}"), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player2, 20);
    }

    private void castCreatureWithMultikicker(List<String> payments) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                payments, false);
    }
}
