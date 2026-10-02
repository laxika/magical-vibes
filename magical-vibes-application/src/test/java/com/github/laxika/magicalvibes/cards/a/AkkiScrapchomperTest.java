package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkkiScrapchomper.class, Forest.class, GrizzlyBears.class, MindStone.class, LiquimetalCoating.class})
class AkkiScrapchomperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact and draws a card")
    void sacrificesArtifactAndDraws() {
        Permanent chomper = addCreatureReady(player1, new AkkiScrapchomper());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        GrizzlyBears draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chomper.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Sacrifices a land and draws a card")
    void sacrificesLandAndDraws() {
        Permanent chomper = addCreatureReady(player1, new AkkiScrapchomper());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        GrizzlyBears draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chomper.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Cannot be activated without an artifact or land to sacrifice")
    void requiresArtifactOrLandToSacrifice() {
        addCreatureReady(player1, new AkkiScrapchomper());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeDrawingOnResolution() {
        addCreatureReady(player1, new AkkiScrapchomper());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        GrizzlyBears draw = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void cannotSacrificeOpponentsPermanents() {
        addCreatureReady(player1, new AkkiScrapchomper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land, artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent chomper = harness.addToBattlefieldAndReturn(player1, new AkkiScrapchomper());
        chomper.setSummoningSick(true);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new AkkiScrapchomper());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent chomper = addCreatureReady(player1, new AkkiScrapchomper());
        chomper.setTapped(true);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(LiquimetalCoating.class)
    void canSacrificeItselfWhenItBecomesAnArtifact() {
        Permanent chomper = addCreatureReady(player1, new AkkiScrapchomper());
        harness.addToBattlefield(player2, new LiquimetalCoating());
        GrizzlyBears draw = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));

        harness.activateAbility(player2, 0, null, chomper.getId());
        harness.passBothPriorities();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chomper);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chomper.getCard());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
