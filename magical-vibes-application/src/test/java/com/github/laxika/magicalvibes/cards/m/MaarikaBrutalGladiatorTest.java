package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ColossusOfSardia;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaarikaBrutalGladiator.class, ColossusOfSardia.class, Forest.class, GrizzlyBears.class, HowlingMine.class, PreyUpon.class, WrathOfGod.class, LlanowarElves.class, PropheticPrism.class, Vorstclaw.class})
class MaarikaBrutalGladiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Maarika must be blocked if able")
    void mustBeBlockedIfAble() {
        Permanent maarika = addCreatureReady(player1, new MaarikaBrutalGladiator());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        maarika.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Maarika has indestructible during its controller's turn only")
    void indestructibleDuringControllerTurnOnly() {
        Permanent maarika = addCreatureReady(player1, new MaarikaBrutalGladiator());
        maarika.setMarkedDamage(4);

        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(maarika);

        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(maarika);
    }

    @Test
    @DisplayName("Maarika's excess-damage trigger sacrifices a noncreature, nonland permanent")
    void sacrificesNoncreatureNonlandPermanentAfterExcessDamage() {
        Permanent maarika = addCreatureReady(player1, new MaarikaBrutalGladiator());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(
                maarika.getId(), harness.getPermanentId(player2, "Llanowar Elves")));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Prophetic Prism");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Maarika does not trigger when its damage is not excess")
    void doesNotTriggerWithoutExcessDamage() {
        Permanent maarika = addCreatureReady(player1, new MaarikaBrutalGladiator());
        harness.addToBattlefield(player2, new Vorstclaw());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(
                maarika.getId(), harness.getPermanentId(player2, "Vorstclaw")));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Maarika, Brutal Gladiator");
        harness.assertInGraveyard(player2, "Vorstclaw");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
    }
}
