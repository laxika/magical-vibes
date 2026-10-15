package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniVengeant;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.cards.l.LushGrowth;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scourglass.class, CylianElf.class, ObeliskOfBant.class, Plains.class,
        EtheriumSculptor.class, LushGrowth.class, AjaniVengeant.class})
class ScourglassTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys non-artifact non-land permanents and sacrifices Scourglass")
    void destroysNonArtifactNonLandPermanents() {
        Permanent scourglass = addScourglassReady(player1);

        addCreatureReady(player1, new CylianElf());

        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new ObeliskOfBant());

        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Creature destroyed
        harness.assertInGraveyard(player1, "Cylian Elf");
        // Artifact survives
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(obelisk);
        // Land survives
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(plains);
        // Scourglass sacrificed as cost
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scourglass);
        harness.assertInGraveyard(player1, "Scourglass");
    }

    @Test
    @DisplayName("Cannot activate during precombat main phase")
    void cannotActivateOutsideUpkeep() {
        addScourglassReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot activate during opponent's upkeep")
    void cannotActivateDuringOpponentUpkeep() {
        addScourglassReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Artifact creatures survive; enchantments are destroyed")
    void sparesArtifactCreaturesDestroysEnchantments() {
        addScourglassReady(player1);

        Permanent sculptor = addCreatureReady(player2, new EtheriumSculptor());

        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent growth = harness.addToBattlefieldAndReturn(player2, new LushGrowth());
        growth.setAttachedTo(plains.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(sculptor);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(growth);
        harness.assertInGraveyard(player2, "Lush Growth");
    }

    @Test
    @DisplayName("Scourglass is sacrificed immediately, but creatures remain until resolution")
    void sacrificesAsCostBeforeDestruction() {
        Permanent scourglass = addScourglassReady(player1);
        harness.addToBattlefield(player2, new CylianElf());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scourglass);
        harness.assertInGraveyard(player1, "Scourglass");
        harness.assertOnBattlefield(player2, "Cylian Elf");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Tapped Scourglass cannot activate and is not sacrificed")
    void cannotActivateWhenTapped() {
        Permanent scourglass = addScourglassReady(player1);
        scourglass.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Scourglass");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly controlled noncreature Scourglass may activate during upkeep")
    void noncreatureIgnoresSummoningSickness() {
        Permanent scourglass = harness.addToBattlefieldAndReturn(player1, new Scourglass());
        scourglass.setSummoningSick(true);
        harness.addToBattlefield(player2, new CylianElf());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scourglass");
        harness.assertInGraveyard(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Nonartifact planeswalkers and creatures on both battlefields are destroyed")
    void destroysPlaneswalkerAndBothPlayersCreatures() {
        addScourglassReady(player1);
        harness.addToBattlefield(player1, new CylianElf());
        harness.addToBattlefield(player2, new CylianElf());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniVengeant());
        ajani.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cylian Elf");
        harness.assertInGraveyard(player2, "Cylian Elf");
        harness.assertInGraveyard(player2, "Ajani Vengeant");
    }

    private Permanent addScourglassReady(Player player) {
        return addCreatureReady(player, new Scourglass());
    }
}
