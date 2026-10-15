package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BastionMastodon;
import com.github.laxika.magicalvibes.cards.s.ServoExhibition;
import com.github.laxika.magicalvibes.cards.t.ThrivingIbex;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WispweaverAngel.class, BastionMastodon.class, ServoExhibition.class, ThrivingIbex.class})
class WispweaverAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may flicker another creature you control")
    void etbMayFlickerAnotherCreatureYouControl() {
        Permanent mastodon = addCreatureReady(player1, new BastionMastodon());
        var oldMastodonId = mastodon.getId();

        castAngel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, oldMastodonId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returnedMastodon = findPermanent(player1, "Bastion Mastodon");
        assertThat(returnedMastodon.getId()).isNotEqualTo(oldMastodonId);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target on the battlefield")
    void decliningEtbLeavesTargetOnBattlefield() {
        Permanent mastodon = addCreatureReady(player1, new BastionMastodon());

        castAngel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mastodon.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Bastion Mastodon").getId()).isEqualTo(mastodon.getId());
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent mastodon = addCreatureReady(player2, new BastionMastodon());
        harness.castFromHand(player1, new WispweaverAngel(), "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Bastion Mastodon").getId()).isEqualTo(mastodon.getId());
    }

    @Test
    @DisplayName("The Angel cannot target itself when it is the only creature")
    void cannotTargetItself() {
        castAngel();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wispweaver Angel");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A borrowed creature returns under its owner's control")
    void borrowedCreatureReturnsToOwner() {
        Permanent mastodon = harness.addToBattlefieldAndReturn(player2, new BastionMastodon());
        gd.playerBattlefields.get(player2.getId()).remove(mastodon);
        gd.playerBattlefields.get(player1.getId()).add(mastodon);
        gd.stolenCreatures.put(mastodon.getId(), player2.getId());

        castAngel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mastodon.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Bastion Mastodon");
        assertThat(findPermanent(player2, "Bastion Mastodon").getId()).isNotEqualTo(mastodon.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger still flickers its target after the Angel leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent mastodon = harness.addToBattlefieldAndReturn(player1, new BastionMastodon());
        castAngel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mastodon.getId());
        Permanent angel = findPermanent(player1, "Wispweaver Angel");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, angel));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Bastion Mastodon").getId()).isNotEqualTo(mastodon.getId());
        harness.assertInGraveyard(player1, "Wispweaver Angel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that changes to the opponent's control is not flickered")
    void targetBecomesIllegalWhenControlChanges() {
        Permanent mastodon = harness.addToBattlefieldAndReturn(player1, new BastionMastodon());
        castAngel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mastodon.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mastodon);
        gd.playerBattlefields.get(player2.getId()).add(mastodon);
        gd.stolenCreatures.put(mastodon.getId(), player1.getId());

        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Bastion Mastodon").getId()).isEqualTo(mastodon.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flickering a token exiles it without returning it")
    void tokenDoesNotReturn() {
        harness.castFromHand(player1, new ServoExhibition(), "{1}{W}");
        harness.passBothPriorities();
        Permanent servo = findPermanent(player1, "Servo");
        assertThat(countPermanents(player1, "Servo")).isEqualTo(2);

        castAngel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, servo.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(servo);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The returning creature is untapped, has no old counters, and is summoning sick")
    void returnedCreatureHasFreshPermanentState() {
        Permanent mastodon = addCreatureReady(player1, new BastionMastodon());
        mastodon.tap();
        mastodon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castAngel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, mastodon.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Bastion Mastodon");
        assertThat(returned.getId()).isNotEqualTo(mastodon.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Returning a creature triggers its enter-the-battlefield ability")
    void returnedCreatureTriggersEnterAbility() {
        Permanent ibex = harness.addToBattlefieldAndReturn(player1, new ThrivingIbex());
        gd.playerEnergyCounters.put(player1.getId(), 0);

        castAngel();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ibex.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Thriving Ibex").getId()).isNotEqualTo(ibex.getId());
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private void castAngel() {
        harness.castFromHand(player1, new WispweaverAngel(), "{4}{W}{W}");
    }
}
