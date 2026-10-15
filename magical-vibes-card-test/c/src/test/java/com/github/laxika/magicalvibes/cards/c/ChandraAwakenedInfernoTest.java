package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PyromancersGauntlet;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraAwakenedInferno.class, Cancel.class, GrizzlyBears.class, FireElemental.class,
        Murder.class, PyromancersGauntlet.class})
class ChandraAwakenedInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("The planeswalker spell cannot be countered")
    void cannotBeCountered() {
        ChandraAwakenedInferno chandra = new ChandraAwakenedInferno();
        harness.setHand(player1, List.of(chandra));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chandra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chandra, Awakened Inferno");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("+2 gives the opponent an upkeep damage emblem")
    void plusTwoCreatesEmblem() {
        Permanent chandra = addReadyChandra(player1, 4);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("-3 damages non-Elemental creatures only")
    void minusThreeSkipsElementals() {
        Permanent chandra = addReadyChandra(player1, 4);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FireElemental());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fire Elemental");
    }

    @Test
    @DisplayName("-X exiles a creature that would die from the damage")
    void minusXExilesLethalCreatureDamage() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, 3, bear.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("-X cannot target a player")
    void minusXCannotTargetPlayer() {
        addReadyChandra(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 3, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroDamageDoesNotExileCreatureDestroyedLater() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new FireElemental());

        harness.activateAbility(player1, 0, 2, 0, elemental.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Fire Elemental");
        destroyElemental(elemental);

        harness.assertInGraveyard(player2, "Fire Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .doesNotContain("Fire Elemental");
    }

    @Test
    void nonlethalDamageExilesCreatureDestroyedLaterThisTurn() {
        addReadyChandra(player1, 4);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new FireElemental());

        harness.activateAbility(player1, 0, 2, 1, elemental.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Fire Elemental");
        destroyElemental(elemental);

        harness.assertNotOnBattlefield(player2, "Fire Elemental");
        harness.assertNotInGraveyard(player2, "Fire Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Fire Elemental");
    }

    @Test
    void minusXExilesPlaneswalkerReducedToZeroLoyalty() {
        Permanent target = addReadyChandra(player2, 3);
        Permanent source = addReadyChandra(player1, 4);

        harness.activateAbility(player1, 0, 2, 3, target.getId());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Chandra, Awakened Inferno");
        harness.assertNotInGraveyard(player2, "Chandra, Awakened Inferno");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Chandra, Awakened Inferno");
    }

    @Test
    void minusThreeAlsoDamagesFriendlyCreaturesButNotPlayers() {
        addReadyChandra(player1, 4);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FireElemental());
        int firstLife = gd.playerLifeTotals.get(player1.getId());
        int secondLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fire Elemental");
        harness.assertLife(player1, firstLife);
        harness.assertLife(player2, secondLife);
    }

    @Test
    void emblemDamageIsNotDamageFromARedPlaneswalker() {
        addReadyChandra(player1, 4);
        harness.addToBattlefield(player2, new PyromancersGauntlet());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
    }

    @Test
    void minusXStillResolvesWhenPayingAllLoyaltyRemovesChandra() {
        addReadyChandra(player1, 4);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new FireElemental());

        harness.activateAbility(player1, 0, 2, 4, elemental.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra, Awakened Inferno");
        harness.assertNotOnBattlefield(player2, "Fire Elemental");
        harness.assertNotInGraveyard(player2, "Fire Elemental");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .contains("Fire Elemental");
    }

    @Test
    void emblemsTriggerSeparatelyOnlyOnTheirControllersUpkeep() {
        addReadyChandra(player1, 4);
        int firstLife = gd.playerLifeTotals.get(player1.getId());
        int secondLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.assertLife(player1, firstLife);
        harness.assertLife(player2, secondLife);
        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player1, firstLife);
        harness.assertLife(player2, secondLife - 2);
    }

    private void destroyElemental(Permanent elemental) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraAwakenedInferno());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
