package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ApexHawks;
import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.t.TasteOfParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RumblingAftershocks.class, ApexHawks.class, BurstLightning.class, TasteOfParadise.class})
class RumblingAftershocksTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToTheNumberOfMultikickerPayments() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{W}", "{1}{W}"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Apex Hawks");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Apex Hawks");
    }

    @Test
    void dealsOneDamageForARegularKickedSpell() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.setHand(player1, List.of(new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castKickedInstant(player1, 0, player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 19);
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

    @Test
    void doesNotTriggerForAnUnkickedMultikickerSpell() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Apex Hawks");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerForOpponentsKickedSpell() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ApexHawks()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player2, 0, List.of("{1}{W}"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Apex Hawks");
        harness.assertLife(player1, 20);
    }

    @Test
    void canDeclineDamageWhenTheTargetedTriggerResolves() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{W}"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Apex Hawks");
    }

    @Test
    void canDealLethalDamageToACreatureBeforeTheKickedSpellResolves() {
        harness.addToBattlefield(player1, new RumblingAftershocks());
        harness.addToBattlefield(player2, new ApexHawks());
        harness.setHand(player1, List.of(new ApexHawks()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{1}{W}", "{1}{W}"));

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Apex Hawks"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Apex Hawks");
        harness.assertNotOnBattlefield(player2, "Apex Hawks");
        harness.assertNotOnBattlefield(player1, "Apex Hawks");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Apex Hawks");
    }
}
