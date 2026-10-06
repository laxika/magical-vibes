package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.c.CompositeGolem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntingWind.class, AetherSpellbomb.class, CompositeGolem.class, GrizzlyBears.class,
        IcyManipulator.class, IronMyr.class, Ornithopter.class, SongOfTheDryads.class, ShivanDragon.class})
class HauntingWindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an artifact you control deals 1 damage to its controller")
    void tappingOwnArtifactDealsDamageToItsController() {
        harness.addToBattlefield(player1, new HauntingWind());
        addCreatureReady(player1, new IronMyr());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping an opponent's artifact deals 1 damage to its controller")
    void tappingOpponentArtifactDealsDamageToItsController() {
        harness.addToBattlefield(player1, new HauntingWind());
        addCreatureReady(player2, new IronMyr());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Activating an artifact's non-tap ability deals damage to its controller")
    void activatingOwnArtifactNonTapAbilityDealsDamage() {
        harness.addToBattlefield(player1, new HauntingWind());
        harness.addToBattlefield(player1, new AetherSpellbomb());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Activating an opponent's artifact non-tap ability deals damage to its controller")
    void activatingOpponentArtifactNonTapAbilityDealsDamage() {
        harness.addToBattlefield(player1, new HauntingWind());
        harness.addToBattlefield(player2, new AetherSpellbomb());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Activating an artifact's non-tap mana ability deals damage")
    void activatingArtifactNonTapManaAbilityDealsDamage() {
        harness.addToBattlefield(player1, new HauntingWind());
        harness.addToBattlefield(player2, new CompositeGolem());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A tap-cost artifact ability causes only the tap trigger")
    void tapCostAbilityDoesNotCauseActivationTrigger() {
        harness.addToBattlefield(player1, new HauntingWind());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void activatingANonartifactAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new HauntingWind());
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.addToBattlefield(player2, new ShivanDragon());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void tappingArtifactWithAnEffectTriggersForBothArtifacts() {
        harness.addToBattlefield(player1, new HauntingWind());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    void tappingAnAlreadyTappedArtifactDoesNotTriggerAgain() {
        harness.addToBattlefield(player1, new HauntingWind());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        target.tap();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    void losingPrintedAbilitiesStopsOwnArtifactActivationTrigger() {
        Permanent wind = harness.addToBattlefieldAndReturn(player1, new HauntingWind());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, wind.getId());
        resolveAllTriggers();
        harness.addToBattlefield(player1, new CompositeGolem());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void losingPrintedAbilitiesStopsOpponentArtifactActivationTrigger() {
        Permanent wind = harness.addToBattlefieldAndReturn(player1, new HauntingWind());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, wind.getId());
        resolveAllTriggers();
        harness.addToBattlefield(player2, new CompositeGolem());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }
}
