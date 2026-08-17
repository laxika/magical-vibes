package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheloniteMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a green creature makes the target land a Forest indefinitely")
    void targetLandBecomesForestIndefinitely() {
        addReadyMonk();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, mountain.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodder.getCard());
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.FOREST);

        mountain.resetModifiers();

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("The Monk can sacrifice itself to pay its ability")
    void monkCanSacrificeItself() {
        Permanent monk = addReadyMonk();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(monk);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(monk.getCard());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("The ability cannot target a nonland permanent")
    void cannotTargetNonland() {
        addReadyMonk();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addReadyMonk() {
        Permanent monk = harness.addToBattlefieldAndReturn(player1, new TheloniteMonk());
        monk.setSummoningSick(false);
        return monk;
    }
}
