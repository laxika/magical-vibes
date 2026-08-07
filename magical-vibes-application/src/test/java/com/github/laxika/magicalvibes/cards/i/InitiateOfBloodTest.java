package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InitiateOfBloodTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    private Permanent addReadyInitiate(Player player) {
        Permanent perm = new Permanent(new InitiateOfBlood());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Deals 1 damage to a damaged creature and flips when that creature dies")
    void killsDamagedCreatureAndFlips() {
        Permanent initiate = addReadyInitiate(player1);
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveStack();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(initiate.isTransformed()).isTrue();
        assertThat(initiate.getCard().getName()).isEqualTo("Goka the Unjust");
    }

    @Test
    @DisplayName("Does not flip while the damaged creature survives")
    void doesNotFlipWhenTargetSurvives() {
        Permanent initiate = addReadyInitiate(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveStack();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(initiate.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips when something else finishes off the damaged creature later in the turn")
    void flipsWhenAnotherSourceKillsTheTarget() {
        Permanent initiate = addReadyInitiate(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveStack();
        assertThat(initiate.isTransformed()).isFalse();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        resolveStack();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(initiate.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        addReadyInitiate(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Once flipped, Goka deals 4 damage to a damaged creature")
    void flippedGokaDeals4Damage() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setTransformed(true);
        initiate.setCard(initiate.getOriginalCard().getBackFaceCard());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.activateAbility(player1, 0, null, targetId);
        resolveStack();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
