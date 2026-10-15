package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HammerheadShark;
import com.github.laxika.magicalvibes.cards.z.ZombieInfestation;
import com.github.laxika.magicalvibes.cards.s.ShabrazTheSkyshark;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrallinSkysharkRider.class, GrizzlyBears.class, HammerheadShark.class, ZombieInfestation.class, ShabrazTheSkyshark.class})
class BrallinSkysharkRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Shabraz")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card shabraz = new ShabrazTheSkyshark();
        harness.setLibrary(player2, List.of(shabraz));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new BrallinSkysharkRider());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(shabraz);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding puts a +1/+1 counter on Brallin and damages each opponent")
    void discardTriggerAddsCounterAndDamagesOpponent() {
        Permanent brallin = harness.addToBattlefieldAndReturn(player1, new BrallinSkysharkRider());
        harness.addToBattlefield(player1, new ZombieInfestation());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(brallin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The activated ability grants trample to a Shark until end of turn")
    void grantsTrampleToTargetShark() {
        harness.addToBattlefield(player1, new BrallinSkysharkRider());
        Permanent shark = harness.addToBattlefieldAndReturn(player1, new HammerheadShark());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, shark.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shark, Keyword.TRAMPLE)).isTrue();

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, shark, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target a non-Shark")
    void rejectsNonSharkTarget() {
        Permanent brallin = harness.addToBattlefieldAndReturn(player1, new BrallinSkysharkRider());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, brallin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Shark");
    }

    @Test
    @DisplayName("The target player may decline the partner search")
    void targetPlayerMayDeclinePartnerSearch() {
        Card shabraz = new ShabrazTheSkyshark();
        harness.setLibrary(player2, List.of(shabraz));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new BrallinSkysharkRider());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(shabraz);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Partner search can target Brallin's controller and find no matching card")
    void partnerSearchCanTargetControllerWithoutMatchingCard() {
        Card otherCard = new BrallinSkysharkRider();
        harness.setLibrary(player1, List.of(otherCard));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new BrallinSkysharkRider());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's discards do not trigger Brallin")
    void opponentsDiscardsDoNotTriggerBrallin() {
        Permanent brallin = harness.addToBattlefieldAndReturn(player1, new BrallinSkysharkRider());
        harness.addToBattlefield(player2, new ZombieInfestation());
        harness.setHand(player2, List.of(new BrallinSkysharkRider(), new ShabrazTheSkyshark()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(brallin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Brallin can grant trample to an opponent's Shark")
    void grantsTrampleToOpponentsShark() {
        harness.addToBattlefield(player1, new BrallinSkysharkRider());
        Permanent shark = harness.addToBattlefieldAndReturn(player2, new ShabrazTheSkyshark());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, shark.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shark, Keyword.TRAMPLE)).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
