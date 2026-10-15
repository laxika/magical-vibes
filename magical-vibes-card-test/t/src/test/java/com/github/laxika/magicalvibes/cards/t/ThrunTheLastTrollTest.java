package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.q.QuilledSlagwurm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrunTheLastTroll.class, Cancel.class, GiantGrowth.class, GoForTheThroat.class,
        ProdigalPyromancer.class, QuilledSlagwurm.class, Shock.class, SpiketailHatchling.class})
class ThrunTheLastTrollTest extends BaseCardTest {


    @Test
    @DisplayName("Thrun cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        ThrunTheLastTroll thrun = new ThrunTheLastTroll();
        harness.setHand(player1, List.of(thrun));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, thrun.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thrun, the Last Troll");
        harness.assertNotInGraveyard(player1, "Thrun, the Last Troll");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Thrun cannot be countered by counter-unless-pays abilities")
    void cannotBeCounteredByCounterUnlessPays() {
        harness.addToBattlefield(player2, new SpiketailHatchling());

        ThrunTheLastTroll thrun = new ThrunTheLastTroll();
        harness.setHand(player1, List.of(thrun));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, thrun.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Thrun, the Last Troll");
    }


    @Test
    @DisplayName("Opponent cannot target Thrun with spells")
    void opponentCannotTargetWithSpells() {
        Permanent thrunPerm = addCreatureReady(player1, new ThrunTheLastTroll());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, thrunPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target own Thrun with spells")
    void controllerCanTargetOwnThrun() {
        Permanent thrunPerm = addCreatureReady(player1, new ThrunTheLastTroll());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, thrunPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Opponent cannot target Thrun with activated abilities")
    void opponentCannotTargetWithActivatedAbilities() {
        Permanent thrun = addCreatureReady(player1, new ThrunTheLastTroll());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, thrun.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }


    @Test
    @DisplayName("Activating regeneration puts it on the stack")
    void activatingRegenPutsOnStack() {
        Permanent thrunPerm = addCreatureReady(player1, new ThrunTheLastTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(thrunPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new ThrunTheLastTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent thrun = findPermanent(player1, "Thrun, the Last Troll");
        assertThat(thrun.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Thrun from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent thrunPerm = addCreatureReady(player1, new ThrunTheLastTroll());
        thrunPerm.setRegenerationShield(1);
        thrunPerm.setBlocking(true);
        thrunPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new QuilledSlagwurm());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Thrun, the Last Troll");
        Permanent thrun = findPermanent(player1, "Thrun, the Last Troll");
        assertThat(thrun.isTapped()).isTrue();
        assertThat(thrun.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Thrun dies without regeneration shield")
    void diesWithoutRegenShield() {
        Permanent thrunPerm = addCreatureReady(player1, new ThrunTheLastTroll());
        thrunPerm.setBlocking(true);
        thrunPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new QuilledSlagwurm());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Thrun, the Last Troll");
        harness.assertInGraveyard(player1, "Thrun, the Last Troll");
    }

    @Test
    @DisplayName("Regeneration can be activated while Thrun is tapped and summoning sick")
    void regeneratesWhileTappedAndSummoningSick() {
        Permanent thrun = harness.addToBattlefieldAndReturn(player1, new ThrunTheLastTroll());
        thrun.setSummoningSick(true);
        thrun.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thrun.getRegenerationShield()).isEqualTo(1);
        assertThat(thrun.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Thrun, the Last Troll");
    }

    @Test
    @DisplayName("One regeneration shield replaces only one destruction")
    void oneShieldSavesFromOnlyOneDestruction() {
        Permanent thrun = addCreatureReady(player1, new ThrunTheLastTroll());
        harness.setHand(player1, List.of(new GoForTheThroat(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(thrun.isTapped()).isFalse();

        harness.castInstant(player1, 0, thrun.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thrun, the Last Troll");
        assertThat(thrun.isTapped()).isTrue();
        assertThat(thrun.getRegenerationShield()).isZero();

        harness.castInstant(player1, 0, thrun.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thrun, the Last Troll");
        harness.assertInGraveyard(player1, "Thrun, the Last Troll");
    }

}
