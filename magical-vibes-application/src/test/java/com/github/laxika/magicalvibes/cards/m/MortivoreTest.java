package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mortivore.class, GrizzlyBears.class, Plains.class, MindRot.class, GloriousAnthem.class})
class MortivoreTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mortivore puts it on the stack")
    void castingPutsItOnStack() {
        Mortivore card = new Mortivore();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Resolving Mortivore puts it on the battlefield when graveyard has creatures")
    void resolvingPutsItOnBattlefield() {
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setHand(player1, List.of(new Mortivore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mortivore");
    }

    @Test
    @DisplayName("Mortivore dies to state-based actions when no creatures in any graveyard")
    void diesWhenNoCreaturesInGraveyards() {
        harness.setHand(player1, List.of(new Mortivore()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA
        harness.assertNotOnBattlefield(player1, "Mortivore");
        harness.assertInGraveyard(player1, "Mortivore");
    }

    @Test
    @DisplayName("Mortivore is 0/0 with no creature cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = addCreatureReady(player1, new Mortivore());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Mortivore P/T equals number of creature cards in controller's graveyard")
    void ptEqualsCreatureCountInOwnGraveyard() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mortivore P/T counts creature cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setGraveyard(player2, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Mortivore only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent perm = addCreatureReady(player1, new Mortivore());

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Plains());
        graveyard.add(new MindRot());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mortivore P/T updates when creatures are added to graveyard")
    void ptUpdatesWhenCreaturesAddedToGraveyard() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mortivore P/T decreases when creatures are removed from graveyard")
    void ptDecreasesWhenCreaturesRemovedFromGraveyard() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(5));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);

        gd.playerGraveyards.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mortivore P/T counts opponent's graveyard creatures too")
    void ptCountsOpponentsGraveyard() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mortivore P/T works with large graveyard counts")
    void ptWorksWithLargeGraveyardCounts() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(15));
        harness.setGraveyard(player2, createCreatureCards(10));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(25);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(25);
    }

    @Test
    @DisplayName("Mortivore P/T stacks with other static bonuses")
    void ptStacksWithOtherStaticBonuses() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(3));

        // Add a Glorious Anthem for +1/+1 to own creatures
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mortivore P/T stacks with temporary power modifiers")
    void ptStacksWithTemporaryModifiers() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(3));

        perm.setPowerModifier(2);
        perm.setToughnessModifier(2);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activating regeneration puts ability on stack with self as target")
    void activatingRegenPutsOnStack() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(perm.getCard());
        assertThat(entry.getTargetId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(perm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can stack multiple regeneration shields")
    void canStackMultipleRegenShields() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(perm.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate regeneration ability while Mortivore is tapped")
    void canActivateRegenerationWhileTapped() {
        Permanent perm = addCreatureReady(player1, new Mortivore());
        perm.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(perm.getId());
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate regeneration ability with only nonblack mana")
    void cannotActivateRegenerationWithOnlyNonblackMana() {
        addCreatureReady(player1, new Mortivore());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Mortivore from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent mortPerm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(2)); // Mortivore is 2/2
        mortPerm.setRegenerationShield(1);
        mortPerm.setBlocking(true);
        mortPerm.addBlockingTarget(0);

        // Grizzly Bears (2/2) deals 2 damage — lethal to 2-toughness Mortivore
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Mortivore should survive via regeneration
        harness.assertOnBattlefield(player1, "Mortivore");
        Permanent mort = findPermanent(player1, "Mortivore");
        assertThat(mort.isTapped()).isTrue();
        assertThat(mort.getRegenerationShield()).isEqualTo(0);
        // Grizzly Bears also takes 2 lethal damage from Mortivore and dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mortivore dies without regeneration shield when taking lethal combat damage")
    void diesWithoutRegenShield() {
        Permanent mortPerm = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(2)); // Mortivore is 2/2
        mortPerm.setBlocking(true);
        mortPerm.addBlockingTarget(0);

        // Grizzly Bears (2/2) deals 2 damage — lethal to 2-toughness Mortivore
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Both creatures trade — Mortivore has no regeneration shield
        harness.assertNotOnBattlefield(player1, "Mortivore");
        harness.assertInGraveyard(player1, "Mortivore");
    }

    @Test
    @DisplayName("Mortivore going to graveyard increases other Mortivore's P/T")
    void dyingMortivoreIncreasesOtherMortivorePT() {
        Permanent mort1 = addCreatureReady(player1, new Mortivore());
        Permanent mort2 = addCreatureReady(player1, new Mortivore());
        harness.setGraveyard(player1, createCreatureCards(3)); // Both are 3/3

        assertThat(gqs.getEffectivePower(gd, mort1)).isEqualTo(3);

        // Simulate mort2 dying — move its card to graveyard
        gd.playerBattlefields.get(player1.getId()).remove(mort2);
        gd.playerGraveyards.get(player1.getId()).add(mort2.getCard());

        // mort1 should now be 4/4 (3 original creatures + Mortivore in graveyard)
        assertThat(gqs.getEffectivePower(gd, mort1)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mort1)).isEqualTo(4);
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }
}

