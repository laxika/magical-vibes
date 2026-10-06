package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeFindsAWay.class, AirElemental.class, GrizzlyBears.class, GloriousAnthem.class})
class LifeFindsAWayTest extends BaseCardTest {

    @Test
    @DisplayName("Populates after a nontoken creature with power 4 or greater enters")
    void populatesAfterHighPowerNontokenCreatureEnters() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        harness.addToBattlefield(player1, soldierToken());
        harness.addToBattlefield(player1, creatureToken(2, 2));
        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Soldier Token"));

        assertThat(countOf(player1, "Air Elemental")).isEqualTo(1);
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a nontoken creature with power less than 4")
    void doesNotTriggerForLowPowerNontokenCreature() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countOf(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a creature token with power 4 or greater")
    void doesNotTriggerForHighPowerCreatureToken() {
        harness.addToBattlefield(player1, new LifeFindsAWay());

        harness.enterBattlefieldAndReturn(player1, creatureToken(4, 4));

        assertThat(gd.stack).isEmpty();
        assertThat(countOf(player1, "Creature Token")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's high-power creature")
    void doesNotTriggerForOpponentsCreature() {
        harness.addToBattlefield(player1, new LifeFindsAWay());

        harness.enterBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gd.stack).isEmpty();
        assertThat(countOf(player2, "Air Elemental")).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolves without creating a token when only the opponent controls a creature token")
    void doesNothingWithoutCreatureTokensYouControl() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        harness.addToBattlefield(player2, soldierToken());

        harness.enterBattlefieldAndReturn(player1, new AirElemental());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countOf(player1, "Soldier Token")).isZero();
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Automatically copies the only creature token without triggering again for its high power")
    void automaticallyCopiesOnlyTokenWithoutRetriggering() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        harness.addToBattlefield(player1, creatureToken(4, 4));

        harness.enterBattlefieldAndReturn(player1, new AirElemental());
        harness.passBothPriorities();

        assertThat(countOf(player1, "Creature Token")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Copies the token's original characteristics without its counters or tapped state")
    void doesNotCopyCountersOrTappedState() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        Permanent original = harness.addToBattlefieldAndReturn(player1, soldierToken());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.enterBattlefieldAndReturn(player1, new AirElemental());
        harness.passBothPriorities();

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Soldier Token".equals(permanent.getCard().getName()))
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getPlusOnePlusOneCounters()).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(1);
    }

    @Test
    @DisplayName("Still populates after the entering creature's power falls below four")
    void doesNotRecheckEnteringCreaturePowerOnResolution() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        harness.addToBattlefield(player1, soldierToken());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new AirElemental());
        assertThat(gd.stack).hasSize(1);
        entering.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Uses power including continuous bonuses when the creature enters")
    void triggersForCreatureBoostedToFourPowerAsItEnters() {
        harness.addToBattlefield(player1, new LifeFindsAWay());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, soldierToken());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private long countOf(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }

    private static Card soldierToken() {
        Card card = creatureToken(1, 1);
        card.setName("Soldier Token");
        card.setColor(CardColor.WHITE);
        return card;
    }

    private static Card creatureToken(int power, int toughness) {
        Card card = new Card();
        card.setName("Creature Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
