package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrismariTheInspiration.class, DarkRitual.class, GrizzlyBears.class, LightningBolt.class,
        Hurricane.class, ProdigalSorcerer.class, Frogify.class})
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

    @Test
    void sorceryGetsStormAndCopiesPreserveX() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new PrismariTheInspiration());

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Prismari, the Inspiration");
        harness.assertInGraveyard(player1, "Hurricane");
    }

    @Test
    void opponentsSpellCountsButDoesNotGetStorm() {
        harness.addToBattlefield(player1, new PrismariTheInspiration());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    void firstSpellHasNoStormCopies() {
        harness.addToBattlefield(player1, new PrismariTheInspiration());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Dark Ritual");
    }

    @Test
    void wardAllowsSpellWhenFiveLifeIsPaid() {
        var prismari = harness.addToBattlefieldAndReturn(player1, new PrismariTheInspiration());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, prismari.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 15);
        harness.passBothPriorities();

        assertThat(prismari.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Prismari, the Inspiration");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    void wardCountersWhenControllerCannotPayFiveLife() {
        var prismari = harness.addToBattlefieldAndReturn(player1, new PrismariTheInspiration());
        harness.setLife(player2, 4);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, prismari.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 4);
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(prismari.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        var prismari = harness.addToBattlefieldAndReturn(player1, new PrismariTheInspiration());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, prismari.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(prismari.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardAlsoCountersAnOpponentsActivatedAbility() {
        var prismari = harness.addToBattlefieldAndReturn(player1, new PrismariTheInspiration());
        addCreatureReady(player2, new ProdigalSorcerer());

        harness.activateAbility(player2, 0, null, prismari.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(prismari.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Prodigal Sorcerer");
    }

    @Test
    void stormCopyCanChooseANewTarget() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new PrismariTheInspiration());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellCopiesDoNotIncreaseStormCount() {
        harness.addToBattlefield(player1, new PrismariTheInspiration());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    void losingAbilitiesToAnAuraStopsGrantingStorm() {
        var prismari = harness.addToBattlefieldAndReturn(player1, new PrismariTheInspiration());
        harness.setHand(player1, List.of(new Frogify(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, prismari.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dark Ritual");
    }
}
