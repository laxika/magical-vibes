package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.e.ElementalAppeal;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandraAblaze.class, Shock.class, Opt.class, ElementalAppeal.class})
class ChandraAblazeTest extends BaseCardTest {

    @Test
    @DisplayName("+1 deals 4 damage when a red card is discarded")
    void plusOneDealsDamageForRedDiscard() {
        Permanent chandra = addReadyChandra(player1, 5);
        harness.setHand(player1, List.of(new Shock()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
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

        harness.activateAbility(player1, 0, 0, player2.getId(), null);
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
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraAblaze());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    @Test
    @DisplayName("+1 can resolve with an empty hand and deals no damage")
    void plusOneWithEmptyHand() {
        Permanent chandra = addReadyChandra(player1, 5);
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+1 does not discard if its only target leaves the battlefield")
    void plusOneIllegalTargetDoesNotDiscard() {
        addReadyChandra(player1, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraAblaze());
        target.setCounterCount(CounterType.LOYALTY, 5);
        Shock card = new Shock();
        harness.setHand(player1, List.of(card));

        harness.activateAbility(player1, 0, 0, target.getId(), null);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-2 draws three cards for players whose hands are empty")
    void minusTwoWithEmptyHands() {
        addReadyChandra(player1, 5);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("-7 allows declining all eligible cards")
    void minusSevenCanCastZeroCards() {
        addReadyChandra(player1, 8);
        Shock card = new Shock();
        harness.setGraveyard(player1, List.of(card));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-7 casts multiple red spells including a sorcery, which return to the graveyard")
    void minusSevenCastsInstantAndSorcery() {
        addReadyChandra(player1, 8);
        ElementalAppeal sorcery = new ElementalAppeal();
        Shock instant = new Shock();
        harness.setGraveyard(player1, List.of(sorcery, instant));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(sorcery, instant);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertOnBattlefield(player1, "Elemental");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sorcery, instant);
    }
}
