package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SorceressQueen;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Planeswalkerificate.class, GrizzlyBears.class, GiantGrowth.class, Shock.class, SorceressQueen.class, Forest.class})
class PlaneswalkerificateTest extends BaseCardTest {

    @Test
    @DisplayName("Turns a creature into a planeswalker with toughness-based loyalty abilities")
    void grantsPlaneswalkerAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        assertThat(gqs.isCreature(gd, creature)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, creature)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(com.github.laxika.magicalvibes.model.ManaColor.RED))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("The variable loyalty cost reduces toughness instead of loyalty counters")
    void variableLoyaltyCostReducesToughness() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 2, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(creature.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY)).isZero();
        harness.assertLife(player2, 19);
    }

    @Test
    void minusOneAllowsCastingTheExiledCardWithItsNormalCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void minusOneAllowsPlayingALandWithoutGrantingAnExtraLandPlay() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new Forest()));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEnchantAnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Planeswalkerificate()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilePermissionExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void enchantedCreatureCannotBlock() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);

        assertThat(bls.canBlock(gd, creature)).isFalse();
    }

    @Test
    void loyaltyAbilitiesShareOneActivationPerTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void spendingAllToughnessStillResolvesDamageAfterTheCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        harness.activateAbility(player1, 0, 2, 2, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Planeswalkerificate");
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotSpendMoreThanTheCurrentToughness() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void damageReducesToughnessAndDoesNotHealAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void temporaryToughnessCanBeSpentButItsExpirationCanKillTheCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.activateAbility(player1, 0, 2, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        harness.assertLife(player2, 16);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Planeswalkerificate");
    }

    @Test
    void aSecondAuraDoesNotResetToughnessChangedByALoyaltyCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        castAura(creature);

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void baseToughnessSettingDoesNotPreventPayingLoyaltyCosts() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        addCreatureReady(player1, new SorceressQueen());
        harness.activateAbility(player1, 2, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    void baseToughnessSettingDoesNotPreventDamageFromReducingToughness() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castAura(creature);
        addCreatureReady(player1, new SorceressQueen());
        harness.activateAbility(player1, 2, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Planeswalkerificate");
    }

    private void castAura(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Planeswalkerificate()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Planeswalkerificate());
        aura.setAttachedTo(creature.getId());
    }
}
