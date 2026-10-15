package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.s.SacredCat;
import com.github.laxika.magicalvibes.cards.s.SplendidAgony;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NestOfScarabs.class, AirElemental.class, Skinrender.class,
        Colossapede.class, SacredCat.class, SplendidAgony.class})
class NestOfScarabsTest extends BaseCardTest {

    /** Drives the stack to completion (Nest's tokens are mandatory — no prompts). Bounded so a stuck
     *  state fails fast instead of hanging. */
    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty(); guard++) {
            harness.passBothPriorities();
        }
    }

    private long insectTokenCount(Player player) {
        return countPermanents(player, "Insect");
    }

    @Test
    @DisplayName("You put three -1/-1 counters on a creature — create three Insect tokens")
    void createsThatManyTokensWhenYouPlaceCounters() {
        harness.addToBattlefield(player1, new NestOfScarabs());
        // 4/4 survives three -1/-1 counters (becomes 1/1), so no death interferes.
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        // player1 casts Skinrender → player1 puts three -1/-1 counters → Nest triggers.
        harness.castCreature(player1, 0, targetId);
        resolveStack();

        assertThat(insectTokenCount(player1)).isEqualTo(3);
        assertThat(insectTokenCount(player2)).isZero();
    }

    @Test
    @DisplayName("An opponent putting the -1/-1 counters does not trigger your Nest of Scarabs")
    void doesNotTriggerWhenOpponentPlacesCounters() {
        harness.addToBattlefield(player1, new NestOfScarabs());
        // The creature receiving the counters belongs to player1 so player2's Skinrender has a target.
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        // player2 casts Skinrender → player2 (not player1) puts the counters → Nest must not trigger.
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0, targetId);
        resolveStack();

        assertThat(insectTokenCount(player1)).isZero();
    }

    @Test
    void multipleCountersProduceOneTriggerThatCreatesAllTokensTogether() {
        harness.addToBattlefield(player1, new NestOfScarabs());
        var creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(insectTokenCount(player1)).isZero();
        harness.passBothPriorities();
        assertThat(insectTokenCount(player1)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersOnTwoCreaturesProduceOneTriggerPerCreature() {
        harness.addToBattlefield(player1, new NestOfScarabs());
        var ownCreature = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        var opposingCreature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gd.stack).hasSize(2);
        resolveStack();
        assertThat(insectTokenCount(player1)).isEqualTo(2);
        assertThat(insectTokenCount(player2)).isZero();
    }

    @Test
    void createsAllTokensEvenWhenCountersKillTheCreature() {
        harness.addToBattlefield(player1, new NestOfScarabs());
        var creature = harness.addToBattlefieldAndReturn(player2, new SacredCat());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));
        resolveStack();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(insectTokenCount(player1)).isEqualTo(2);
    }

    @Test
    void eachNestCreatesItsOwnTokens() {
        harness.addToBattlefield(player1, new NestOfScarabs());
        harness.addToBattlefield(player1, new NestOfScarabs());
        harness.addToBattlefield(player2, new NestOfScarabs());
        var creature = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));
        resolveStack();

        assertThat(insectTokenCount(player1)).isEqualTo(4);
        assertThat(insectTokenCount(player2)).isZero();
    }
}
