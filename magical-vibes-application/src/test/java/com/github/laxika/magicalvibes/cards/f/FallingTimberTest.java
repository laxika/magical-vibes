package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.cards.s.SilverDrake;
import com.github.laxika.magicalvibes.cards.s.SlingshotGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallingTimber.class, AlphaKavu.class, MeteorCrater.class, SilverDrake.class, SlingshotGoblin.class})
class FallingTimberTest extends BaseCardTest {

    @Test
    void preventsCombatDamageByTargetCreature() {
        Permanent target = addCreatureReady(player2, new AlphaKavu());
        addCreatureReady(player2, new AlphaKavu());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void kickedFallingTimberPreventsCombatDamageByTwoTargetCreaturesAndSacrificesLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        Permanent firstTarget = addCreatureReady(player2, new AlphaKavu());
        Permanent secondTarget = addCreatureReady(player2, new AlphaKavu());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false, land.getId(), null,
                null, null, null, true);
        harness.passBothPriorities();
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Meteor Crater");
    }

    @Test
    void kickedFallingTimberRequiresAnotherTargetCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        Permanent target = addCreatureReady(player2, new AlphaKavu());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(target.getId(), target.getId()), List.of(), false, land.getId(), null,
                null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void preventsCombatDamageButNotNoncombatDamageByTargetCreature() {
        Permanent source = addCreatureReady(player2, new SlingshotGoblin());
        Permanent target = addCreatureReady(player2, new SilverDrake());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void preventsTargetCreatureCombatDamageToABlocker() {
        Permanent attacker = addCreatureReady(player2, new AlphaKavu());
        Permanent blocker = addCreatureReady(player1, new AlphaKavu());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, attacker.getId());
        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
    }

    @Test
    void kickerRequiresSacrificingALand() {
        Permanent nonland = addCreatureReady(player1, new AlphaKavu());
        Permanent firstTarget = addCreatureReady(player2, new AlphaKavu());
        Permanent secondTarget = addCreatureReady(player2, new AlphaKavu());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false, nonland.getId(), null,
                null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonland);
        assertThat(gd.stack).isEmpty();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
