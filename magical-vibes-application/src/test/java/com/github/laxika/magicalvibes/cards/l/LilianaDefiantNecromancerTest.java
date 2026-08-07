package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LilianaDefiantNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("+2 makes each player discard a card and raises loyalty")
    void plusTwoEachPlayerDiscards() {
        Permanent liliana = addReadyLiliana(player1, 3);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-X returns a nonlegendary creature card whose mana value equals X")
    void minusXReanimatesMatchingManaValue() {
        Permanent liliana = addReadyLiliana(player1, 5);
        Card bears = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("-X cannot target a creature card whose mana value differs from X")
    void minusXRejectsWrongManaValue() {
        addReadyLiliana(player1, 5);
        Card giant = new HillGiant(); // mana value 4
        harness.setGraveyard(player1, List.of(giant));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, giant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-X cannot target a legendary creature card")
    void minusXRejectsLegendaryCreature() {
        addReadyLiliana(player1, 5);
        Card legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.setGraveyard(player1, List.of(legendaryBears));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, legendaryBears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 emblem returns a dying creature under your control at the next end step")
    void minusEightEmblemReanimatesDyingCreatures() {
        addReadyLiliana(player1, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities(); // Shock resolves, Bears dies
        harness.passBothPriorities(); // the emblem's trigger resolves, scheduling the return

        // The return is delayed to the beginning of the next end step.
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent perm = new Permanent(new LilianaDefiantNecromancer());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
