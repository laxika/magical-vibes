package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KeenEyedArchers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TadeasJuniperAscendant.class, GrizzlyBears.class, KeenEyedArchers.class})
class TadeasJuniperAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof unless it is attacking")
    void hasHexproofUnlessAttacking() {
        Permanent tadeas = addCreatureReady(player1, new TadeasJuniperAscendant());

        assertThat(gqs.hasKeyword(gd, tadeas, Keyword.HEXPROOF)).isTrue();

        tadeas.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, tadeas, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Reach attacker is untapped and cannot be blocked by greater-power creatures this combat")
    void reachAttackerGetsCombatRestriction() {
        Permanent tadeas = addCreatureReady(player1, new TadeasJuniperAscendant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        assertThat(tadeas.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(tadeas.isTapped()).isFalse();

        prepareDeclareBlockers(player1);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(bears),
                gd.playerBattlefields.get(player1.getId()).indexOf(tadeas)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another reach creature uses its own power for the restriction")
    void anotherReachCreatureUsesItsOwnPower() {
        addCreatureReady(player1, new TadeasJuniperAscendant());
        Permanent archer = addCreatureReady(player1, new KeenEyedArchers());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(bears),
                gd.playerBattlefields.get(player1.getId()).indexOf(archer))));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("One or more creatures dealing combat damage draws one card")
    void combatDamageDrawsOneCard() {
        addCreatureReady(player1, new TadeasJuniperAscendant());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void simultaneousDamageFromSeveralCreaturesDrawsOnlyOneCard() {
        addCreatureReady(player1, new TadeasJuniperAscendant());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new KeenEyedArchers());
        Card drawn = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertLife(player2, 15);
    }

    @Test
    void nonReachAttackerStaysTappedButStillTriggersDraw() {
        addCreatureReady(player1, new TadeasJuniperAscendant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertLife(player2, 18);
    }

    @Test
    void blockedReachCreatureDoesNotDrawForDamageToCreature() {
        addCreatureReady(player1, new TadeasJuniperAscendant());
        Permanent archer = addCreatureReady(player1, new KeenEyedArchers());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(archer.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
        harness.assertLife(player2, 20);
    }

    @Test
    void lowerPowerCreatureCanBlockReachAttacker() {
        addCreatureReady(player1, new TadeasJuniperAscendant());
        addCreatureReady(player1, new KeenEyedArchers());
        Permanent blocker = addCreatureReady(player2, new TadeasJuniperAscendant());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
