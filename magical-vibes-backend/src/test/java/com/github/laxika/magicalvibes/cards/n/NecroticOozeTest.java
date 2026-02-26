package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecroticOozeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Necrotic Ooze has static effect to gain activated abilities from graveyard creatures")
    void hasCorrectStaticEffect() {
        NecroticOoze card = new NecroticOoze();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GainActivatedAbilitiesOfCreatureCardsInAllGraveyardsEffect.class);
    }

    // ===== Gaining abilities from graveyard creatures =====

    @Test
    @DisplayName("Gains activated ability from creature card in controller's graveyard")
    void gainsAbilityFromOwnGraveyard() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().getManaCost()).isEqualTo("{B}");
    }

    @Test
    @DisplayName("Gains activated ability from creature card in opponent's graveyard")
    void gainsAbilityFromOpponentGraveyard() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player2, new ArrayList<>(List.of(new ProdigalPyromancer())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Gains abilities from creature cards in all graveyards combined")
    void gainsAbilitiesFromAllGraveyards() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new ProdigalPyromancer())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).hasSize(2);
    }

    @Test
    @DisplayName("Does not gain abilities from non-creature cards in graveyard")
    void doesNotGainAbilitiesFromNonCreatureCards() {
        Permanent ooze = addOozeReady(player1);
        // Rod of Ruin is a non-creature artifact with an activated ability
        harness.setGraveyard(player1, new ArrayList<>(List.of(new RodOfRuin())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).isEmpty();
    }

    @Test
    @DisplayName("Does not gain abilities from vanilla creature cards with no activated abilities")
    void noAbilitiesFromVanillaCreatures() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).isEmpty();
    }

    @Test
    @DisplayName("Only gains abilities from creature cards, ignoring non-creatures in mixed graveyard")
    void onlyGainsFromCreaturesInMixedGraveyard() {
        Permanent ooze = addOozeReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new DrudgeSkeletons());  // creature with ability
        graveyard.add(new GrizzlyBears());     // creature without ability
        graveyard.add(new RodOfRuin());        // non-creature artifact with ability
        harness.setGraveyard(player1, graveyard);

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().getManaCost()).isEqualTo("{B}");
    }

    // ===== Dynamic updates =====

    @Test
    @DisplayName("Gains new abilities when creature cards are added to graveyard")
    void gainsAbilitiesWhenCreatureAddedToGraveyard() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>());

        assertThat(gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities()).isEmpty();

        gd.playerGraveyards.get(player1.getId()).add(new ProdigalPyromancer());

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();
        assertThat(granted).hasSize(1);
    }

    @Test
    @DisplayName("Loses abilities when creature cards are removed from graveyard")
    void losesAbilitiesWhenCreatureRemovedFromGraveyard() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));

        assertThat(gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities()).hasSize(1);

        gd.playerGraveyards.get(player1.getId()).clear();

        assertThat(gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities()).isEmpty();
    }

    // ===== Activating gained abilities =====

    @Test
    @DisplayName("Can activate a non-tap ability gained from a graveyard creature")
    void canActivateGainedNonTapAbility() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Ability index 0 = the first granted ability (Drudge Skeletons' regenerate)
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Necrotic Ooze");
    }

    @Test
    @DisplayName("Can activate a tap ability gained from a graveyard creature")
    void canActivateGainedTapAbility() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new ProdigalPyromancer())));

        // Create a target creature on opponent's battlefield
        Permanent target = new Permanent(new GrizzlyBears());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Necrotic Ooze");
        assertThat(ooze.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving gained tap damage ability deals damage to target")
    void resolvedGainedTapAbilityDealsDamage() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new ProdigalPyromancer())));

        // Target the opponent directly
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Resolving gained regenerate ability grants regeneration shield to Ooze")
    void resolvedGainedRegenerateAbilityGrantsShield() {
        Permanent ooze = addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ooze.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate abilities when no creatures with abilities in graveyard")
    void cannotActivateWhenNoAbilitiesInGraveyard() {
        addOozeReady(player1);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Multiple abilities =====

    @Test
    @DisplayName("Can choose between multiple gained abilities by ability index")
    void canChooseBetweenMultipleGainedAbilities() {
        Permanent ooze = addOozeReady(player1);
        // DrudgeSkeletons has {B}: Regenerate (non-tap), ProdigalPyromancer has {T}: deal 1 damage (tap)
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons(), new ProdigalPyromancer())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Activate first gained ability (Drudge Skeletons' regenerate)
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ooze.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Helper methods =====

    private Permanent addOozeReady(Player player) {
        NecroticOoze card = new NecroticOoze();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
