package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SporewebWeaver.class, ProdigalSorcerer.class, Shock.class, ConcordiaPegasus.class})
class SporewebWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("When Sporeweb Weaver is dealt damage, its controller gains life and creates a Saproling")
    void dealtDamageGainsLifeAndCreatesSaproling() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player2, new SporewebWeaver());
        Permanent pinger = addCreatureReady(player2, new ProdigalSorcerer());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pinger), null,
                weaver.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 21);

        List<Permanent> tokens = findPermanents(player2, "Saproling");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    void opponentsRedSpellCanTargetWeaver() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player2, new SporewebWeaver());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, weaver.getId());
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
        harness.passBothPriorities();

        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
    }

    @Test
    void opponentsBlueAbilityCannotTargetWeaver() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new SporewebWeaver());
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, weaver.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    void separateDamageEventsEachTriggerIncludingLethalDamage() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new SporewebWeaver());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, weaver.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        harness.assertOnBattlefield(player1, "Sporeweb Weaver");

        harness.castAndResolveInstant(player1, 0, weaver.getId());
        harness.assertInGraveyard(player1, "Sporeweb Weaver");
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    void blocksFlyingAttackerAndTriggersFromCombatDamage() {
        addCreatureReady(player1, new ConcordiaPegasus());
        Permanent weaver = addCreatureReady(player2, new SporewebWeaver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(weaver.isBlocking()).isTrue();
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 21);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Sporeweb Weaver");
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
    }
}
