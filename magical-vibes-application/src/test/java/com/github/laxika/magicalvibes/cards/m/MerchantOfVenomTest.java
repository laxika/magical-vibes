package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantOfVenom.class, GrizzlyBears.class, TerramorphicExpanse.class})
class MerchantOfVenomTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each player sacrifices a creature and Merchant of Venom grows")
    void eachPlayerSacrificesCreatureAndMerchantGrows() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMerchantOfVenom();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ownBears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Merchant of Venom");

        resolveSacrificeTriggers();

        Permanent merchant = findPermanent(player1, "Merchant of Venom");
        assertThat(merchant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A player with no other creature sacrifices nothing on entry")
    void playerWithNoOtherCreatureSacrificesNothing() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMerchantOfVenom();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, ownBears.getId());
        resolveSacrificeTriggers();

        harness.assertOnBattlefield(player1, "Merchant of Venom");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Merchant of Venom")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Merchant must sacrifice itself when it is its controller's only creature")
    void sacrificesItselfAndTriggersForItsOwnSacrifice() {
        castMerchantOfVenom();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Merchant of Venom");
        harness.assertInGraveyard(player1, "Merchant of Venom");
        assertThat(gd.stack).hasSize(1);
        resolveSacrificeTriggers();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A sacrificed Merchant sees itself and the opponent's simultaneous sacrifice")
    void sacrificedMerchantTriggersForBothSimultaneousSacrifices() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMerchantOfVenom();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Merchant of Venom");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(2);
        resolveSacrificeTriggers();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both players choose before any creature is sacrificed")
    void sacrificesWaitForBothPlayersChoices() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingOtherBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMerchantOfVenom();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, ownBears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBears);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingBears, opposingOtherBears);

        harness.handlePermanentChosen(player2, opposingBears.getId());
        resolveSacrificeTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingOtherBears);
        assertThat(findPermanent(player1, "Merchant of Venom")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a land as an activation cost gives Merchant a counter")
    void controllerLandSacrificeTriggersCounter() {
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new MerchantOfVenom());
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 1, null, null);
        harness.assertInGraveyard(player1, "Terramorphic Expanse");
        harness.passBothPriorities();

        assertThat(merchant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's land sacrifice also gives Merchant a counter")
    void opponentLandSacrificeTriggersCounter() {
        Permanent merchant = harness.addToBattlefieldAndReturn(player1, new MerchantOfVenom());
        harness.addToBattlefield(player2, new TerramorphicExpanse());
        harness.setLibrary(player2, List.of());
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.assertInGraveyard(player2, "Terramorphic Expanse");
        harness.passBothPriorities();

        assertThat(merchant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castMerchantOfVenom() {
        harness.castFromHand(player1, new MerchantOfVenom(), "{3}{B}");
    }

    private void resolveSacrificeTriggers() {
        GameData gameData = harness.getGameData();
        while (!gameData.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
