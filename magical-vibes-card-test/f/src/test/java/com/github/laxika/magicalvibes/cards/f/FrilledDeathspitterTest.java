package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.a.AngrathTheFlameChained;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrilledDeathspitter.class, Shock.class, FanaticalFirebrand.class, AngrathTheFlameChained.class})
class FrilledDeathspitterTest extends BaseCardTest {

    @Test
    void spellDamageTriggersEnrage() {
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID deathspitterId = harness.getPermanentId(player2, "Frilled Deathspitter");
        harness.castAndResolveInstant(player1, 0, deathspitterId);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Frilled Deathspitter");
    }

    @Test
    void eachSeparateDamageEventTriggersEvenWhenTheSecondIsLethal() {
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.addToBattlefield(player1, new FanaticalFirebrand());
        harness.addToBattlefield(player1, new FanaticalFirebrand());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        UUID deathspitterId = harness.getPermanentId(player2, "Frilled Deathspitter");

        harness.activateAbility(player1, 0, null, deathspitterId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Frilled Deathspitter");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);

        harness.activateAbility(player1, 0, null, deathspitterId);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Frilled Deathspitter");
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void enrageCanTargetEitherPlayersPlaneswalker(boolean ownPlaneswalker) {
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.addToBattlefield(player1, new FanaticalFirebrand());
        var planeswalkerController = ownPlaneswalker ? player2 : player1;
        Permanent angrath = harness.addToBattlefieldAndReturn(planeswalkerController, new AngrathTheFlameChained());
        angrath.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Frilled Deathspitter"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, angrath.getId());
        harness.passBothPriorities();

        assertThat(angrath.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Frilled Deathspitter");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void controllerChoosesWhichOpponentIsDealtDamage() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefield(player2, new FrilledDeathspitter());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Frilled Deathspitter"));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player3.getId());
        harness.handlePermanentChosen(player2, player3.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player3, 18);
    }

    @Test
    void controllerChoosesWhichOpponentIsDealtDamageAfterCombatDamage() {
        Player player3 = addThirdPlayer();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FanaticalFirebrand());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FrilledDeathspitter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player3.getId());
        harness.handlePermanentChosen(player2, player3.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player3, 18);
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }

    @Test
    void combatDamageTriggersEnrage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FanaticalFirebrand());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FrilledDeathspitter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Frilled Deathspitter");
        harness.assertInGraveyard(player1, "Fanatical Firebrand");
    }
}
