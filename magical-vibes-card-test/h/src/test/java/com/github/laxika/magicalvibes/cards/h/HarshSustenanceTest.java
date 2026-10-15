package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UginTheSpiritDragon;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarshSustenance.class, GrizzlyBears.class, AirElemental.class, UginTheSpiritDragon.class})
class HarshSustenanceTest extends BaseCardTest {

    @Test
    @DisplayName("Harsh Sustenance deals damage to a player and gains life based on creatures controlled")
    void dealsDamageToPlayerAndGainsLifeBasedOnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new HarshSustenance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Harsh Sustenance deals damage to a creature and gains life")
    void dealsDamageToCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new HarshSustenance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Air Elemental"));

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Harsh Sustenance counts only creatures controlled by its caster at resolution")
    void countsOnlyCreaturesControlledByCasterAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HarshSustenance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Harsh Sustenance does nothing if its target is illegal on resolution")
    void fizzlesWhenTargetCreatureIsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HarshSustenance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Harsh Sustenance deals no damage and gains no life with no creatures")
    void zeroCreaturesDealsNoDamageAndGainsNoLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareHarshSustenance();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Harsh Sustenance includes creatures entering after it is cast")
    void countsCreaturesEnteringBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareHarshSustenance();
        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Harsh Sustenance excludes creatures leaving before resolution")
    void excludesCreaturesLeavingBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareHarshSustenance();
        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lethal damage to your own creature does not reduce Harsh Sustenance's life gain")
    void gainsFullLifeWhenOwnTargetDies() {
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareHarshSustenance();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Harsh Sustenance can target a planeswalker and ignores it in the creature count")
    void damagesPlaneswalkerAndGainsLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var target = harness.addToBattlefieldAndReturn(player1, new UginTheSpiritDragon());
        target.setCounterCount(CounterType.LOYALTY, 7);
        prepareHarshSustenance();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Harsh Sustenance can target its caster without losing at zero life during resolution")
    void gainsLifeBeforeCheckingForLoss() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 1);
        prepareHarshSustenance();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 1);
        assertThat(gd.gameResult).isNull();
    }

    private void prepareHarshSustenance() {
        harness.setHand(player1, List.of(new HarshSustenance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
