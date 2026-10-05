package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.SilverMyr;
import com.github.laxika.magicalvibes.cards.y.YixlidJailer;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecroticOoze.class, DrudgeSkeletons.class, GrizzlyBears.class,
        ProdigalPyromancer.class, RodOfRuin.class})
class NecroticOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated ability from creature card in controller's graveyard")
    void gainsAbilityFromOwnGraveyard() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().getManaCost()).isEqualTo("{B}");
    }

    @Test
    @DisplayName("Gains activated ability from creature card in opponent's graveyard")
    void gainsAbilityFromOpponentGraveyard() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player2, new ArrayList<>(List.of(new ProdigalPyromancer())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Gains abilities from creature cards in all graveyards combined")
    void gainsAbilitiesFromAllGraveyards() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new ProdigalPyromancer())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).hasSize(2);
    }

    @Test
    @DisplayName("Does not gain abilities from non-creature cards in graveyard")
    void doesNotGainAbilitiesFromNonCreatureCards() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        // Rod of Ruin is a non-creature artifact with an activated ability
        harness.setGraveyard(player1, new ArrayList<>(List.of(new RodOfRuin())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).isEmpty();
    }

    @Test
    @DisplayName("Does not gain abilities from vanilla creature cards with no activated abilities")
    void noAbilitiesFromVanillaCreatures() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).isEmpty();
    }

    @Test
    @DisplayName("Only gains abilities from creature cards, ignoring non-creatures in mixed graveyard")
    void onlyGainsFromCreaturesInMixedGraveyard() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new DrudgeSkeletons());  // creature with ability
        graveyard.add(new GrizzlyBears());     // creature without ability
        graveyard.add(new RodOfRuin());        // non-creature artifact with ability
        harness.setGraveyard(player1, graveyard);

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().getManaCost()).isEqualTo("{B}");
    }

    @Test
    @DisplayName("Gains new abilities when creature cards are added to graveyard")
    void gainsAbilitiesWhenCreatureAddedToGraveyard() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>());

        assertThat(gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities()).isEmpty();

        gd.playerGraveyards.get(player1.getId()).add(new ProdigalPyromancer());

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities();
        assertThat(granted).hasSize(1);
    }

    @Test
    @DisplayName("Loses abilities when creature cards are removed from graveyard")
    void losesAbilitiesWhenCreatureRemovedFromGraveyard() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));

        assertThat(gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities()).hasSize(1);

        gd.playerGraveyards.get(player1.getId()).clear();

        assertThat(gqs.computeStaticBonus(gd, ooze).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Can activate a non-tap ability gained from a graveyard creature")
    void canActivateGainedNonTapAbility() {
        addCreatureReady(player1, new NecroticOoze());
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
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new ProdigalPyromancer())));

        // Create a target creature on opponent's battlefield
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Necrotic Ooze");
        assertThat(ooze.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving gained tap damage ability deals damage to target")
    void resolvedGainedTapAbilityDealsDamage() {
        addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new ProdigalPyromancer())));

        // Target the opponent directly
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Resolving gained regenerate ability grants regeneration shield to Ooze")
    void resolvedGainedRegenerateAbilityGrantsShield() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ooze.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate abilities when no creatures with abilities in graveyard")
    void cannotActivateWhenNoAbilitiesInGraveyard() {
        addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose between multiple gained abilities by ability index")
    void canChooseBetweenMultipleGainedAbilities() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        // DrudgeSkeletons has {B}: Regenerate (non-tap), ProdigalPyromancer has {T}: deal 1 damage (tap)
        harness.setGraveyard(player1, new ArrayList<>(List.of(new DrudgeSkeletons(), new ProdigalPyromancer())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Activate first gained ability (Drudge Skeletons' regenerate)
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ooze.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @CardUsed(SilverMyr.class)
    @DisplayName("A gained mana ability produces mana immediately without using the stack")
    void gainedManaAbilityResolvesImmediately() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player2, List.of(new SilverMyr()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(ooze.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(FumeSpitter.class)
    @DisplayName("A gained sacrifice cost sacrifices the Ooze rather than the graveyard card")
    void gainedSacrificeAbilityUsesOozeAsSource() {
        Permanent ooze = addCreatureReady(player1, new NecroticOoze());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NecroticOoze());
        FumeSpitter donor = new FumeSpitter();
        harness.setGraveyard(player2, List.of(donor));

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ooze);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ooze.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(donor);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A gained tap ability cannot be activated while the Ooze is summoning sick")
    void gainedTapAbilityRespectsSummoningSickness() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new NecroticOoze());
        ooze.setSummoningSick(true);
        harness.setGraveyard(player2, List.of(new ProdigalPyromancer()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ooze.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the donor card does not stop an already activated ability")
    void activatedAbilitySurvivesDonorLeavingGraveyard() {
        addCreatureReady(player1, new NecroticOoze());
        harness.setGraveyard(player2, List.of(new ProdigalPyromancer()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setGraveyard(player2, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @CardUsed(YixlidJailer.class)
    @DisplayName("Cards stripped of abilities in graveyards cannot grant abilities to the Ooze")
    void graveyardAbilityRemovalPreventsGainingAbilities() {
        addCreatureReady(player1, new NecroticOoze());
        harness.addToBattlefield(player2, new YixlidJailer());
        harness.setGraveyard(player1, List.of(new DrudgeSkeletons()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
