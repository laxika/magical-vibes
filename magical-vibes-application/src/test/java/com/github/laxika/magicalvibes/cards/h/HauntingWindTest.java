package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.c.CompositeGolem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntingWind.class, AetherSpellbomb.class, CompositeGolem.class, GrizzlyBears.class,
        IcyManipulator.class, IronMyr.class})
class HauntingWindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an artifact you control deals 1 damage to its controller")
    void tappingOwnArtifactDealsDamageToItsController() {
        harness.addToBattlefield(player1, new HauntingWind());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        artifact.setSummoningSick(false);
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        resolveStackFully();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping an opponent's artifact deals 1 damage to its controller")
    void tappingOpponentArtifactDealsDamageToItsController() {
        harness.addToBattlefield(player1, new HauntingWind());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IronMyr());
        artifact.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveStackFully();

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
        resolveStackFully();

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
        resolveStackFully();

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
        resolveStackFully();

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
        resolveStackFully();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
