package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({BiteDown.class, GarrukWildspeaker.class, GrizzlyBears.class, HillGiant.class})
class BiteDownTest extends BaseCardTest {

    @Test
    @DisplayName("The creature you control deals damage equal to its power to an opponent's creature")
    void dealsPowerDamageToOpponentCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0,
                List.of(source.getId(), harness.getPermanentId(player2, "Grizzly Bears")));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The creature you control deals damage equal to its power to an opponent's planeswalker")
    void dealsPowerDamageToOpponentPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), planeswalker.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you control as the victim")
    void cannotTargetOwnCreatureAsVictim() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use an opponent's creature as the damage source")
    void cannotUseOpponentsCreatureAsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void usesPowerAtResolutionAndDoesNotDealDamageBack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void dealsNoDamageWhenSourceLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Bite Down");
    }

    @Test
    void dealsNoDamageWhenSourceChangesController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Bite Down");
    }

    @Test
    void dealsNoDamageWhenVictimComesUnderYourControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        gd.playerBattlefields.get(player1.getId()).add(victim);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Bite Down");
    }

    @Test
    void cannotTargetPlayer() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(source.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedCreatureDealsDamageWithoutReceivingDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setTapped(true);
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(source.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void planeswalkerDiesWhenDamageRemovesAllLoyalty() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        victim.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    void cannotTargetOwnPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        victim.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new BiteDown()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
