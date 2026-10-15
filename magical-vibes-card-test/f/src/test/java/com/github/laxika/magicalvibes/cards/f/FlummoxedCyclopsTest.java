package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlummoxedCyclops.class, GrizzlyBears.class, FinalFlare.class})
class FlummoxedCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block after two opponent creatures attack")
    void cannotBlockAfterTwoOpponentCreaturesAttack() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(cyclops.isCantBlockThisCombat()).isTrue();
    }

    @Test
    @DisplayName("Can block when fewer than two opponent creatures attack")
    void canBlockWhenFewerThanTwoOpponentCreaturesAttack() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(cyclops.isCantBlockThisCombat()).isFalse();
    }

    @Test
    @DisplayName("Restriction expires at the end of combat")
    void restrictionExpiresAtEndOfCombat() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();
        resolveCombat(player2);

        assertThat(cyclops.isCantBlockThisCombat()).isFalse();
    }

    @Test
    @DisplayName("Three attacking creatures create only one trigger")
    void threeAttackersCreateOneTrigger() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player2, new FlummoxedCyclops());
        addCreatureReady(player2, new FlummoxedCyclops());
        addCreatureReady(player2, new FlummoxedCyclops());

        declareAttackers(player2, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(cyclops.getId());
        assertThat(cyclops.isCantBlockThisCombat()).isFalse();
    }

    @Test
    @DisplayName("Attacks by the controller do not trigger the restriction")
    void ownCreaturesAttackingDoNotTrigger() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).isEmpty();
        assertThat(cyclops.isCantBlockThisCombat()).isFalse();
    }

    @Test
    @DisplayName("The restriction resolves even if an attacker dies in response")
    void restrictionResolvesAfterAttackerDies() {
        Permanent cyclops = addCreatureReady(player1, new FlummoxedCyclops());
        Permanent sacrifice = addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new FlummoxedCyclops());
        addCreatureReady(player2, new FlummoxedCyclops());
        harness.setHand(player1, List.of(new FinalFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(player2, List.of(0, 1));
        harness.castInstantWithSacrifice(player1, 0, attacker.getId(), sacrifice.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        resolveAllTriggers();

        assertThat(cyclops.isCantBlockThisCombat()).isTrue();
    }

    @Test
    @DisplayName("Attacks against another opponent still trigger the restriction")
    void attacksAgainstAnotherOpponentTrigger() {
        Player observer = addThirdPlayer();
        Permanent cyclops = addCreatureReady(observer, new FlummoxedCyclops());
        addCreatureReady(player1, new FlummoxedCyclops());
        addCreatureReady(player2, new FlummoxedCyclops());
        addCreatureReady(player2, new FlummoxedCyclops());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0, 1)));

        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getSourcePermanentId()).isEqualTo(cyclops.getId()));
    }

    private Player addThirdPlayer() {
        UUID playerId = UUID.randomUUID();
        Player player = new Player(playerId, "Charlie");
        gd.playerIds.add(playerId);
        gd.orderedPlayerIds.add(playerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(playerId, "Charlie");
        gd.playerDecks.put(playerId, new ArrayList<>());
        gd.playerHands.put(playerId, new ArrayList<>());
        gd.playerBattlefields.put(playerId, new ArrayList<>());
        gd.playerGraveyards.put(playerId, new ArrayList<>());
        gd.playerCommandZones.put(playerId, new ArrayList<>());
        gd.playerManaPools.put(playerId, new ManaPool());
        gd.playerLifeTotals.put(playerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), playerId, "Charlie");
        return player;
    }
}
