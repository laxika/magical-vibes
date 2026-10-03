package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectiveEffort.class, GrizzlyBears.class, CampaignOfVengeance.class})
class CollectiveEffortTest extends BaseCardTest {

    @Test
    @DisplayName("Destroy creature mode destroys a creature with power 4 or greater")
    void destroysHighPowerCreature() {
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(5);
        bearCard.setToughness(5);
        Permanent bear = addCreatureReady(player2, bearCard);
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{0}, List.of(bear.getId()), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy enchantment mode destroys an enchantment")
    void destroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CampaignOfVengeance());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{1}, List.of(enchantment.getId()), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Campaign of Vengeance");
        harness.assertInGraveyard(player2, "Campaign of Vengeance");
    }

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on each creature target player controls")
    void putsCountersOnTargetPlayersCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3, new int[]{2}, List.of(player1.getId()), null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple modes tap one creature for each extra mode")
    void multipleModesPayWithCreatureTaps() {
        Permanent firstTapper = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTapper = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(5);
        bearCard.setToughness(5);
        Permanent targetCreature = addCreatureReady(player2, bearCard);
        Permanent targetEnchantment = harness.addToBattlefieldAndReturn(player2, new CampaignOfVengeance());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 3, new int[]{0, 1, 2},
                List.of(targetCreature.getId(), targetEnchantment.getId(), player1.getId()),
                List.of(firstTapper.getId(), secondTapper.getId()));
        harness.passBothPriorities();

        assertThat(firstTapper.isTapped()).isTrue();
        assertThat(secondTapper.isTapped()).isTrue();
        assertThat(firstTapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Campaign of Vengeance");
        harness.assertInGraveyard(player2, "Campaign of Vengeance");
    }

    @Test
    @DisplayName("A creature below power 4 cannot be targeted by the destroy creature mode")
    void rejectsLowPowerCreatureTarget() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 3, new int[]{0}, List.of(bear.getId()), null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Choosing multiple modes without enough creatures to tap is rejected")
    void rejectsMultipleModesWithoutEscalateTaps() {
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(5);
        bearCard.setToughness(5);
        Permanent targetCreature = addCreatureReady(player2, bearCard);
        Permanent targetEnchantment = harness.addToBattlefieldAndReturn(player2, new CampaignOfVengeance());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 3,
                new int[]{0, 1}, List.of(targetCreature.getId(), targetEnchantment.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }


    @Test
    @DisplayName("Counter mode can benefit an opponent")
    void putsCountersOnOpponentsCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{2}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness does not prevent paying the escalate tap cost")
    void canTapSummoningSickCreatureToEscalate() {
        Permanent tapper = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tapper.setSummoningSick(true);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CampaignOfVengeance());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 3,
                new int[]{1, 2}, List.of(enchantment.getId(), player1.getId()), List.of(tapper.getId()));
        assertThat(tapper.isTapped()).isTrue();
        assertThat(tapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Campaign of Vengeance");
        assertThat(tapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An already tapped creature cannot pay the escalate cost")
    void rejectsTappedCreatureForEscalate() {
        Permanent tapper = addCreatureReady(player1, new GrizzlyBears());
        tapper.setTapped(true);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CampaignOfVengeance());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 3,
                new int[]{1, 2}, List.of(enchantment.getId(), player1.getId()), List.of(tapper.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Collective Effort");
        harness.assertOnBattlefield(player2, "Campaign of Vengeance");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("An illegal creature target does not stop the counter mode")
    void resolvesRemainingModeWhenCreaturePowerFallsBelowFour() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(4);
        targetCard.setToughness(4);
        Permanent target = addCreatureReady(player2, targetCard);
        Permanent tapper = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 3,
                new int[]{0, 2}, List.of(target.getId(), player1.getId()), List.of(tapper.getId()));
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(tapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }


    @Test
    @DisplayName("A creature that falls below power four survives when it is the only target")
    void doesNotDestroySoleTargetWhenItsPowerFalls() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(4);
        targetCard.setToughness(4);
        Permanent target = addCreatureReady(player2, targetCard);
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 3,
                new int[]{0}, List.of(target.getId()), null);
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Collective Effort");
    }

    @Test
    @DisplayName("The destruction mode resolves before counters are placed")
    void destroysOwnCreatureBeforePlacingCounters() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setPower(4);
        targetCard.setToughness(4);
        Permanent target = addCreatureReady(player1, targetCard);
        Permanent survivor = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CollectiveEffort()));
        addMana();

        harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 3,
                new int[]{0, 2}, List.of(target.getId(), player1.getId()), List.of(target.getId()));
        assertThat(target.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
