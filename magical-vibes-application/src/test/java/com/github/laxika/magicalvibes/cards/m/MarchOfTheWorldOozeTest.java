package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({MarchOfTheWorldOoze.class, GrizzlyBears.class, Shock.class})
class MarchOfTheWorldOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures become 6/6 Oozes, but an opponent's creature does not")
    void transformsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBear = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownBear)).contains(CardSubtype.OOZE);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingBear)).doesNotContain(CardSubtype.OOZE);
    }

    @Test
    @DisplayName("An opponent's spell during your turn creates an Elephant Ooze")
    void opponentSpellDuringOwnTurnCreatesToken() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        Permanent token = findPermanent(player1, "Elephant");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, token))
                .contains(CardSubtype.ELEPHANT, CardSubtype.OOZE);
    }

    @Test
    @DisplayName("An opponent's spell during their own turn does not create a token")
    void opponentSpellDuringTheirTurnDoesNotCreateToken() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertNotOnBattlefield(player1, "Elephant");
    }

    @Test
    void retainsOriginalSubtypesAndAddsCountersAfterSettingBaseStats() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(8);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bear))
                .contains(CardSubtype.BEAR, CardSubtype.OOZE);

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "March of the World Ooze"));

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bear))
                .contains(CardSubtype.BEAR).doesNotContain(CardSubtype.OOZE);
    }

    @Test
    void controllerSpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player1, "Elephant");
    }

    @Test
    void triggerStillCreatesThreeThreeGreenElephantAfterSourceLeaves() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "March of the World Ooze"));
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elephant");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, token))
                .contains(CardSubtype.ELEPHANT).doesNotContain(CardSubtype.OOZE);
    }

    @Test
    void opponentSpellDuringAnotherOpponentsTurnTriggers() {
        UUID thirdPlayerId = UUID.randomUUID();
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.clear();
        gd.orderedPlayerIds.addAll(List.of(thirdPlayerId, player2.getId(), player1.getId()));
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
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        gd.activePlayerId = thirdPlayerId;
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(2);
    }
}
