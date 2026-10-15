package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.v.VeteransSidearm;
import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MightOfTheMasses.class, DwynensElite.class, VeteransSidearm.class})
class MightOfTheMassesTest extends BaseCardTest {

    @Test
    void zeroCreaturesGivesNoBoostToOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DwynensElite());
        harness.addToBattlefield(player1, new VeteransSidearm());
        harness.setHand(player1, List.of(new MightOfTheMasses()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertInGraveyard(player1, "Might of the Masses");
    }

    @Test
    void countsCreaturesAtResolutionAndBoostDoesNotUpdateAfterward() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DwynensElite());
        harness.addToBattlefield(player1, new VeteransSidearm());
        harness.setHand(player1, List.of(new MightOfTheMasses()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player1, new DwynensElite());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);

        harness.addToBattlefield(player1, new DwynensElite());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost equals the number of creatures the controller controls")
    void boostCountsControllersCreatures() {
        harness.addToBattlefield(player1, new DwynensElite());
        harness.addToBattlefield(player1, new DwynensElite());
        harness.addToBattlefield(player1, new DwynensElite());
        harness.setHand(player1, List.of(new MightOfTheMasses()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Dwynen's Elite");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent's creatures are not counted, and an opponent's creature can be targeted")
    void countsOnlyControllersCreatures() {
        harness.addToBattlefield(player1, new DwynensElite());
        harness.addToBattlefield(player2, new DwynensElite());
        harness.addToBattlefield(player2, new DwynensElite());
        harness.setHand(player1, List.of(new MightOfTheMasses()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Dwynen's Elite");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new DwynensElite());
        harness.addToBattlefield(player1, new DwynensElite());
        harness.setHand(player1, List.of(new MightOfTheMasses()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Dwynen's Elite");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new DwynensElite());
        harness.addToBattlefield(player1, new VeteransSidearm());
        harness.setHand(player1, List.of(new MightOfTheMasses()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Veteran's Sidearm");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
