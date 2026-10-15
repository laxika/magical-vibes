package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalAmulet.class, PrimalWellspring.class, LightningBolt.class, Divination.class, GrizzlyBears.class, Cancel.class})
class PrimalAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Primal Amulet enters the battlefield when its spell resolves")
    void entersBattlefield() {
        harness.setHand(player1, List.of(new PrimalAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Primal Amulet");
    }

    @Test
    @DisplayName("Colored mana requirements remain payable with Primal Amulet")
    void coloredManaRequirementsRemainPayable() {
        addAmuletReady(player1);

        // Lightning Bolt still requires its red mana.
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Should be able to cast with just {R}
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Sorcery spells cost {1} less with Primal Amulet on the battlefield")
    void sorceriesCostOneLess() {
        addAmuletReady(player1);

        // Divination costs {2}{U} — with reduction it costs {1}{U}
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Cost reduction does not apply to creature spells")
    void costReductionDoesNotApplyToCreatures() {
        addAmuletReady(player1);

        // Grizzly Bears costs {1}{G} — should not get reduction
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Only {G} + 0 generic — not enough for {1}{G}

        // Should fail because creature spells don't get the reduction
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> harness.castCreature(player1, 0));
    }

    @Test
    @DisplayName("Casting an instant puts a charge counter on Primal Amulet")
    void castingInstantPutsChargeCounter() {
        Permanent amulet = addAmuletReady(player1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Resolve the counter trigger (on top of stack above the spell)
        harness.passBothPriorities();

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery puts a charge counter on Primal Amulet")
    void castingSorceryPutsChargeCounter() {
        Permanent amulet = addAmuletReady(player1);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);

        // Resolve the counter trigger
        harness.passBothPriorities();

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not put a charge counter on Primal Amulet")
    void castingCreatureDoesNotPutCounter() {
        Permanent amulet = addAmuletReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        // No trigger should fire for creature spells
        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("At 4+ charge counters, player may transform — accepting transforms")
    void transformsWhenAccepted() {
        Permanent amulet = addAmuletReady(player1);
        amulet.setCounterCount(CounterType.CHARGE, 3); // One more needed

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // The transform choice is part of the counter trigger's resolution.
        harness.passBothPriorities();
        // Accept the may transform
        harness.handleMayAbilityChosen(player1, true);

        assertThat(amulet.isTransformed()).isTrue();
        assertThat(amulet.getCard().getName()).isEqualTo("Primal Wellspring");
        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("At 4+ charge counters, player may transform — declining keeps counters")
    void doesNotTransformWhenDeclined() {
        Permanent amulet = addAmuletReady(player1);
        amulet.setCounterCount(CounterType.CHARGE, 3);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // The transform choice is part of the counter trigger's resolution.
        harness.passBothPriorities();
        // Decline the may transform
        harness.handleMayAbilityChosen(player1, false);

        assertThat(amulet.isTransformed()).isFalse();
        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("No may prompt when below 4 charge counters")
    void noMayPromptBelowThreshold() {
        Permanent amulet = addAmuletReady(player1);
        amulet.setCounterCount(CounterType.CHARGE, 2); // Will be 3 after trigger, still below 4

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        // Resolve the counter trigger — adds counter, no may prompt
        harness.passBothPriorities();

        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(amulet.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Wellspring mana copies an instant and the copy deals damage")
    void wellspringCopiesInstantSpell() {
        activateWellspringMana(ManaColor.RED);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Wellspring mana copies a nontargeted sorcery")
    void wellspringCopiesSorcerySpell() {
        activateWellspringMana(ManaColor.BLUE);
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt(),
                new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("One Wellspring mana copies only the spell it pays for")
    void pendingCopyIsOneShot() {
        activateWellspringMana(ManaColor.RED);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Wellspring mana spent on a creature cannot copy a later instant")
    void creatureConsumesWellspringManaWithoutCopying() {
        activateWellspringMana(ManaColor.GREEN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Unused blue Wellspring mana does not copy a red spell paid with other mana")
    void unusedWellspringManaDoesNotCopySpell() {
        activateWellspringMana(ManaColor.BLUE);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Only Wellspring mana actually spent on a spell produces copies")
    void unspentSecondWellspringDoesNotProduceExtraCopy() {
        activateWellspringMana(ManaColor.RED);
        activateWellspringMana(ManaColor.BLUE);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Each Wellspring mana spent on the same spell produces a copy")
    void multipleWellspringManaCopiesSameSpell() {
        activateWellspringMana(ManaColor.BLUE);
        activateWellspringMana(ManaColor.RED);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Divination()));
        harness.castSorcery(player1, 0, 0);
        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @DisplayName("Wellspring mana lost on a step change cannot copy a later spell")
    void pendingCopyClearedOnManaDrain() {
        activateWellspringMana(ManaColor.RED);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent spells neither receive the reduction nor add charge counters")
    void opponentSpellsDoNotBenefitOrTrigger() {
        Permanent amulet = addAmuletReady(player1);
        harness.setHand(player2, List.of(new Divination()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> harness.castSorcery(player2, 0, 0));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Accepting transformation removes every charge counter, including counters above four")
    void transformationRemovesAllChargeCounters() {
        Permanent amulet = addAmuletReady(player1);
        amulet.setCounterCount(CounterType.CHARGE, 5);
        amulet.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(amulet.isTransformed()).isTrue();
        assertThat(amulet.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(amulet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void activateWellspringMana(ManaColor color) {
        Permanent wellspring = addTransformedWellspring(player1);
        harness.activateAbility(player1, indexOf(player1, wellspring), 0, null, null);
        harness.handleListChoice(player1, color.name());
    }

    @Test
    @DisplayName("An instant with a generic cost receives the reduction")
    void instantGenericCostIsReduced() {
        addAmuletReady(player1);
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, bolt.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Wellspring copies a spell even when the original has been countered")
    void copiesCounteredOriginal() {
        activateWellspringMana(ManaColor.RED);
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    private Permanent addAmuletReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PrimalAmulet());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addTransformedWellspring(Player player) {
        PrimalAmulet card = new PrimalAmulet();
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        // Transform to back face
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
