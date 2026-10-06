package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CursedRack;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReliquaryTower.class, GrizzlyBears.class, Forest.class, Mountain.class, Plains.class,
        CursedRack.class})
class ReliquaryTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one colorless mana")
    void tapAddsOneColorlessMana() {
        harness.addToBattlefield(player1, new ReliquaryTower());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller has no maximum hand size — no discard during cleanup")
    void noMaximumHandSizeForController() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new ReliquaryTower());

        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        ));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Opponent's Reliquary Tower does not remove your hand limit")
    void opponentTowerDoesNotHelp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player2, new ReliquaryTower());

        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Plains()
        ));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping Reliquary Tower does not disable its hand-size effect")
    void tappedTowerStillRemovesHandLimit() {
        harness.addToBattlefield(player1, new ReliquaryTower());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new ReliquaryTower(), new ReliquaryTower(),
                new ReliquaryTower(), new ReliquaryTower(), new ReliquaryTower(),
                new ReliquaryTower(), new ReliquaryTower(), new ReliquaryTower()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    @DisplayName("A later Cursed Rack overrides Reliquary Tower's unlimited hand size")
    void laterRackRestoresFiniteHandLimit() {
        harness.addToBattlefieldAndReturn(player1, new ReliquaryTower()).setTimestamp(1);
        var rack = harness.addToBattlefieldAndReturn(player2, new CursedRack());
        rack.setTimestamp(2);
        rack.setRememberedTargetPlayerId(player1.getId());
        harness.setHand(player1, List.of(new ReliquaryTower(), new ReliquaryTower(),
                new ReliquaryTower(), new ReliquaryTower(), new ReliquaryTower()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("A later Reliquary Tower overrides Cursed Rack's finite hand size")
    void laterTowerRemovesRackHandLimit() {
        var rack = harness.addToBattlefieldAndReturn(player2, new CursedRack());
        rack.setTimestamp(1);
        rack.setRememberedTargetPlayerId(player1.getId());
        harness.addToBattlefieldAndReturn(player1, new ReliquaryTower()).setTimestamp(2);
        harness.setHand(player1, List.of(new ReliquaryTower(), new ReliquaryTower(),
                new ReliquaryTower(), new ReliquaryTower(), new ReliquaryTower()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }
}
