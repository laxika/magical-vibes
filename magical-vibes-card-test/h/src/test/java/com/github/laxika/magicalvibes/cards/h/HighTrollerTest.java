package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plummet;
import com.github.laxika.magicalvibes.cards.z.ZuranEnchanter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, ChandrasPyrohelix.class, GrizzlyBears.class, HighTroller.class, Plummet.class,
        ZuranEnchanter.class})
class HighTrollerTest extends BaseCardTest {

    @Test
    void reducesTargetedSpellAndChoosesItsOnlyLegalTargetWithoutATrigger() {
        harness.addToBattlefield(player1, new HighTroller());
        var target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player2, List.of(new Plummet()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
    }

    @Test
    void reducesTargetedActivatedAbilityForAnyPlayer() {
        harness.addToBattlefield(player1, new HighTroller());
        var enchanter = harness.addToBattlefieldAndReturn(player2, new ZuranEnchanter());
        enchanter.setSummoningSick(false);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceNonTargetedSpells() {
        harness.addToBattlefield(player1, new HighTroller());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesItsControllersTargetedSpell() {
        harness.addToBattlefield(player1, new HighTroller());
        var target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotReduceTheColoredManaRequirement() {
        harness.addToBattlefield(player1, new HighTroller());
        var target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void choosesMultipleTargetsWithoutCreatingARandomizationTrigger() {
        harness.addToBattlefield(player1, new HighTroller());
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, Map.of(player1.getId(), 1, player2.getId(), 1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds()).hasSize(2).doesNotHaveDuplicates();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceSpellCostsAfterLosingItsAbilities() {
        var troller = harness.addToBattlefieldAndReturn(player1, new HighTroller());
        troller.setLosesAllAbilitiesUntilEndOfTurn(true);
        var target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRandomizeTargetsAfterLosingItsAbilities() {
        var troller = harness.addToBattlefieldAndReturn(player1, new HighTroller());
        troller.setLosesAllAbilitiesUntilEndOfTurn(true);
        var target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
