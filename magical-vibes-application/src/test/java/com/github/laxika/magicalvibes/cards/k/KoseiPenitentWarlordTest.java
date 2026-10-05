package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoseiPenitentWarlord.class, Bonesplitter.class, Forest.class, HolyStrength.class})
class KoseiPenitentWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted, equipped, and countered, Kosei draws and damages each other opponent")
    void activeConditionsDrawAndDamageOtherOpponents() {
        Player player3 = addThirdPlayer();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        Permanent kosei = addKoseiWithAllConditions();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kosei)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player3.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Kosei does not gain the trigger without a counter")
    void missingCounterDisablesTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        Permanent kosei = addCreatureReady(player1, new KoseiPenitentWarlord());
        attachAuraAndEquipment(kosei);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kosei)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private Permanent addKoseiWithAllConditions() {
        Permanent kosei = addCreatureReady(player1, new KoseiPenitentWarlord());
        kosei.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attachAuraAndEquipment(kosei);
        return kosei;
    }

    @Test
    void missingAuraDisablesTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent kosei = addCreatureReady(player1, new KoseiPenitentWarlord());
        kosei.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(kosei.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 17);
    }

    @Test
    void missingEquipmentDisablesTrigger() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        Permanent kosei = addCreatureReady(player1, new KoseiPenitentWarlord());
        kosei.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(kosei.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void nonPowerCounterEnablesTriggerInTwoPlayerGame() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent kosei = addCreatureReady(player1, new KoseiPenitentWarlord());
        kosei.setCounterCount(CounterType.CHARGE, 1);
        attachAuraAndEquipment(kosei);

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void losingConditionsAfterDamageDoesNotStopTriggerOrChangeItsAmount() {
        Player player3 = addThirdPlayer();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent kosei = addKoseiWithAllConditions();
        kosei.setAttacking(true);
        kosei.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        kosei.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p != kosei);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        harness.assertLife(player3, 16);
    }

    private void attachAuraAndEquipment(Permanent kosei) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(kosei.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(kosei.getId());
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
