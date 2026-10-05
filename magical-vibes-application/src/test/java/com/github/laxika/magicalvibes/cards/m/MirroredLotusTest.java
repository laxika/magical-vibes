package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirroredLotus.class})
class MirroredLotusTest extends BaseCardTest {

    @Test
    void eachOpponentMayPayToCreateACopyWithoutReflect() {
        harness.enterBattlefieldAndReturn(player1, new MirroredLotus());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mirrored Lotus")).isEqualTo(1);
        assertThat(countPermanents(player2, "Mirrored Lotus")).isEqualTo(1);
        Permanent token = findPermanents(player2, "Mirrored Lotus").getFirst();
        assertThat(token.getCard().isToken()).isTrue();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "GREEN");

        assertThat(countPermanents(player2, "Mirrored Lotus")).isZero();
        assertThat(countPermanents(player1, "Mirrored Lotus")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    void reflectOffersPaymentDuringEntryWithoutPassingPriority() {
        harness.enterBattlefieldAndReturn(player1, new MirroredLotus());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void payingReflectQueuesTheCopyInsteadOfCreatingItImmediately() {
        harness.enterBattlefieldAndReturn(player1, new MirroredLotus());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(countPermanents(player2, "Mirrored Lotus")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Mirrored Lotus")).isEqualTo(1);
    }

    @Test
    void opponentMayDeclineTheFreeReflectPayment() {
        harness.enterBattlefieldAndReturn(player1, new MirroredLotus());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(countPermanents(player1, "Mirrored Lotus")).isEqualTo(1);
        assertThat(countPermanents(player2, "Mirrored Lotus")).isZero();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
    @Test
    void tappingAndExilingAddsThreeManaOfTheChosenColor() {
        harness.addToBattlefield(player1, new MirroredLotus());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Mirrored Lotus");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }
}
