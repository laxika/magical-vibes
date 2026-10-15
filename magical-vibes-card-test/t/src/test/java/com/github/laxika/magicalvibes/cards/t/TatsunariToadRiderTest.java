package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarienKingOfKjeldor;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TatsunariToadRider.class, Pacifism.class, GrizzlyBears.class, WindDrake.class,
        GiantSpider.class, DarienKingOfKjeldor.class})
class TatsunariToadRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an enchantment creates a legendary black and green 3/3 Frog named Keimi")
    void castingEnchantmentCreatesKeimi() {
        Permanent keimi = createKeimi();

        assertThat(keimi.getCard().isToken()).isTrue();
        assertThat(keimi.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(keimi.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(keimi.getCard().getSubtypes()).containsExactly(CardSubtype.FROG);
        assertThat(gqs.getEffectivePower(gd, keimi)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, keimi)).isEqualTo(3);
    }

    @Test
    @DisplayName("Keimi drains each opponent and gains its controller life for later enchantments")
    void keimiTriggersForLaterEnchantment() {
        createKeimi();

        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, secondTarget.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(countPermanents(player1, "Keimi")).isEqualTo(1);
    }

    @Test
    @DisplayName("Tatsunari's ability makes Tatsunari and a Frog unblockable except by flying or reach")
    void abilityRestrictsBlockersForTatsunariAndFrog() {
        Permanent keimi = createKeimi();
        Permanent tatsunari = findPermanent(player1, "Tatsunari, Toad Rider");
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, keimi.getId());
        harness.passBothPriorities();

        tatsunari.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying or reach");
    }

    @Test
    @DisplayName("The ability allows a creature with flying to block")
    void flyingCreatureCanBlock() {
        Permanent keimi = createKeimi();
        Permanent tatsunari = findPermanent(player1, "Tatsunari, Toad Rider");
        Permanent flier = addCreatureReady(player2, new WindDrake());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, keimi.getId());
        harness.passBothPriorities();

        tatsunari.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(flier), 0)));

        assertThat(flier.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability only targets a Frog controlled by its activator")
    void abilityCannotTargetNonFrog() {
        harness.addToBattlefield(player1, new TatsunariToadRider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Frog you control");
    }

    @Test
    @DisplayName("The enchantment that creates Keimi does not also drain life")
    void creatingEnchantmentDoesNotTriggerNewKeimi() {
        createKeimi();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Keimi's life loss does not trigger abilities for being dealt damage")
    void keimiLifeLossDoesNotTriggerDarien() {
        createKeimi();
        harness.addToBattlefield(player2, new DarienKingOfKjeldor());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    @DisplayName("Casting a nonenchantment does not trigger Tatsunari or Keimi")
    void creatureSpellDoesNotTriggerEitherAbility() {
        createKeimi();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Keimi")).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's enchantment does not trigger Tatsunari or Keimi")
    void opponentsEnchantmentDoesNotTriggerEitherAbility() {
        createKeimi();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent target = findPermanent(player1, "Tatsunari, Toad Rider");
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Keimi")).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tatsunari creates a new Keimi after the previous one leaves")
    void keimiCanBeRecreated() {
        Permanent original = createKeimi();
        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Keimi")).hasSize(1);
        assertThat(findPermanent(player1, "Keimi").getId()).isNotEqualTo(original.getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's Keimi does not stop Tatsunari from creating yours")
    void opponentsKeimiDoesNotPreventCreation() {
        Permanent original = createKeimi();
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerBattlefields.get(player2.getId()).add(original);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Keimi")).isEqualTo(1);
        assertThat(findPermanents(player2, "Keimi")).containsExactly(original);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tatsunari rechecks that you do not control Keimi when its trigger resolves")
    void creationConditionIsRecheckedOnResolution() {
        Permanent original = createKeimi();
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerBattlefields.get(player2.getId()).add(original);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(original);
        gd.playerBattlefields.get(player1.getId()).add(original);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Keimi")).containsExactly(original);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tatsunari cannot target an opponent's Frog")
    void abilityCannotTargetOpponentsFrog() {
        Permanent keimi = createKeimi();
        gd.playerBattlefields.get(player1.getId()).remove(keimi);
        gd.playerBattlefields.get(player2.getId()).add(keimi);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, keimi.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Frog you control");
    }

    @Test
    @DisplayName("The target Frog also cannot be blocked by a creature without flying or reach")
    void abilityRestrictsBlockersForTargetFrog() {
        Permanent keimi = createKeimi();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, keimi.getId());
        resolveAllTriggers();

        keimi.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying or reach");
    }

    @Test
    @DisplayName("A creature with reach can block the target Frog")
    void reachCreatureCanBlockTargetFrog() {
        Permanent keimi = createKeimi();
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, keimi.getId());
        resolveAllTriggers();

        keimi.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("If the target Frog leaves, Tatsunari does not gain the blocking restriction")
    void illegalFrogTargetMakesEntireAbilityFail() {
        Permanent keimi = createKeimi();
        Permanent tatsunari = findPermanent(player1, "Tatsunari, Toad Rider");
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, keimi.getId());
        gd.playerBattlefields.get(player1.getId()).remove(keimi);
        resolveAllTriggers();

        tatsunari.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Keimi present at cast time prevents creation even if it leaves before resolution")
    void existingKeimiPreventsCreationTriggerAtCastTime() {
        Permanent keimi = createKeimi();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(keimi);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Keimi")).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Tatsunari's creation trigger resolves after Tatsunari leaves")
    void creationTriggerSurvivesTatsunariLeaving() {
        Permanent tatsunari = harness.addToBattlefieldAndReturn(player1, new TatsunariToadRider());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(tatsunari);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Keimi")).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent createKeimi() {
        harness.addToBattlefield(player1, new TatsunariToadRider());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
        return findPermanent(player1, "Keimi");
    }
}
