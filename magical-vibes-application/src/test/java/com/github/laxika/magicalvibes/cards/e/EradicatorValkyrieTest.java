package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FeedTheSerpent;
import com.github.laxika.magicalvibes.cards.g.GoldmawChampion;
import com.github.laxika.magicalvibes.cards.k.KayaTheInexorable;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EradicatorValkyrie.class, GoldmawChampion.class, TyvarKell.class,
        SnowCoveredForest.class, FeedTheSerpent.class, KayaTheInexorable.class})
class EradicatorValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Boast makes each opponent sacrifice a creature")
    void boastSacrificesOpponentsCreature() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        addCreatureReady(player2, new GoldmawChampion());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eradicator Valkyrie");
        harness.assertNotOnBattlefield(player2, "Goldmaw Champion");
    }

    @Test
    @DisplayName("Boast makes each opponent sacrifice a planeswalker")
    void boastSacrificesOpponentsPlaneswalker() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        harness.addToBattlefield(player2, new TyvarKell());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tyvar Kell");
    }

    @Test
    @DisplayName("Boast does not sacrifice noncreature, nonplaneswalker permanents")
    void boastIgnoresLands() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        harness.addToBattlefield(player2, new SnowCoveredForest());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Snow-Covered Forest");
    }

    @Test
    @DisplayName("Boast requires the Valkyrie to have attacked this turn")
    void boastRequiresAttack() {
        addCreatureReady(player1, new EradicatorValkyrie());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        Permanent fodder = addCreatureReady(player1, new GoldmawChampion());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void opponentChoosesBetweenCreatureAndPlaneswalker() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        addCreatureReady(player2, new GoldmawChampion());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TyvarKell());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Eradicator Valkyrie");
        harness.assertOnBattlefield(player2, "Goldmaw Champion");
        harness.assertOnBattlefield(player2, "Tyvar Kell");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        harness.assertInGraveyard(player2, "Tyvar Kell");
        harness.assertOnBattlefield(player2, "Goldmaw Champion");
    }

    @Test
    void sacrificingAnotherCreatureLeavesValkyrieOnBattlefield() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        Permanent fodder = addCreatureReady(player1, new GoldmawChampion());
        addCreatureReady(player2, new GoldmawChampion());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.assertInGraveyard(player1, "Goldmaw Champion");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eradicator Valkyrie");
        harness.assertInGraveyard(player2, "Goldmaw Champion");
    }

    @Test
    void opponentInstantCanTargetValkyrie() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        harness.setHand(player2, List.of(new FeedTheSerpent()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castInstant(player2, 0, valkyrie.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eradicator Valkyrie");
        harness.assertNotInGraveyard(player1, "Eradicator Valkyrie");
    }

    @Test
    void opponentCreatureAbilityCanTargetValkyrie() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        Permanent champion = addCreatureReady(player2, new GoldmawChampion());
        champion.setAttackedThisTurn(true);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, null, valkyrie.getId());
        harness.passBothPriorities();

        assertThat(valkyrie.isTapped()).isTrue();
    }

    @Test
    void opponentPlaneswalkerCannotTargetValkyrie() {
        Permanent valkyrie = addCreatureReady(player2, new EradicatorValkyrie());
        harness.addToBattlefield(player1, new KayaTheInexorable());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, valkyrie.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Eradicator Valkyrie");
    }

    @Test
    void ownPlaneswalkerCanTargetValkyrie() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        harness.addToBattlefield(player1, new KayaTheInexorable());

        harness.activateAbility(player1, 1, 1, null, valkyrie.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eradicator Valkyrie");
        harness.assertNotInGraveyard(player1, "Eradicator Valkyrie");
    }

    @Test
    void unblockedCombatDamageGainsLife() {
        addCreatureReady(player1, new EradicatorValkyrie());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void creatureWithoutFlyingCannotBlockValkyrie() {
        addCreatureReady(player1, new EradicatorValkyrie());
        addCreatureReady(player2, new GoldmawChampion());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boastRequiresBlackMana() {
        Permanent valkyrie = addCreatureReady(player1, new EradicatorValkyrie());
        valkyrie.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Eradicator Valkyrie");
    }
}
