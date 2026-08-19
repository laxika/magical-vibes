package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FallingTimberTest extends BaseCardTest {

    @Test
    void preventsCombatDamageByTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void kickedFallingTimberPreventsCombatDamageByTwoTargetCreaturesAndSacrificesLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false, land.getId(), null,
                null, null, null, true
        );
        harness.passBothPriorities();
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void kickedFallingTimberRequiresAnotherTargetCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, null, null,
                List.of(target.getId(), target.getId()), List.of(), false, land.getId(), null,
                null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FallingTimber()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
