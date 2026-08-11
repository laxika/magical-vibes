package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChandraAblazeTest extends BaseCardTest {

    @Test
    @DisplayName("+1 deals 4 damage when a red card is discarded")
    void plusOneDealsDamageForRedDiscard() {
        Permanent chandra = addReadyChandra(player1, 5);
        harness.setHand(player1, List.of(new Shock()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("+1 does not deal damage when a non-red card is discarded")
    void plusOneDoesNotDealDamageForNonRedDiscard() {
        Permanent chandra = addReadyChandra(player1, 5);
        harness.setHand(player1, List.of(new Opt()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Opt");
    }

    @Test
    @DisplayName("-2 makes each player discard their hand and draw three cards")
    void minusTwoDiscardsHandsAndDrawsThree() {
        Permanent chandra = addReadyChandra(player1, 5);
        harness.setHand(player1, List.of(new Shock(), new Opt()));
        harness.setHand(player2, List.of(new Opt()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Opt");
    }

    @Test
    @DisplayName("-7 offers red instants and sorceries in the graveyard for free")
    void minusSevenCastsRedInstantFromGraveyardWithoutPaying() {
        Permanent chandra = addReadyChandra(player1, 7);
        Shock shock = new Shock();
        Opt opt = new Opt();
        harness.setGraveyard(player1, List.of(shock, opt));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Opt");
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ChandraAblaze());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
