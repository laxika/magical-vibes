package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneyardMycodrax.class, GrizzlyBears.class, Mountain.class, Terminate.class})
class BoneyardMycodraxTest extends BaseCardTest {

    @Test
    @DisplayName("Boneyard Mycodrax's power and toughness count other creature cards in its controller's graveyard")
    void powerAndToughnessCountOtherCreatureCardsInOwnGraveyard() {
        Permanent mycodrax = addCreatureReady(player1, new BoneyardMycodrax());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Mountain()));

        assertThat(gqs.getEffectivePower(gd, mycodrax)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mycodrax)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Mycodrax's scavenge uses its graveyard power")
    void scavengeUsesGraveyardPower() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new BoneyardMycodrax(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Boneyard Mycodrax");
    }

    @Test
    void powerAndToughnessIgnoreOpponentsGraveyardAndUpdateWithOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new BoneyardMycodrax()));
        harness.setGraveyard(player2, List.of(new BoneyardMycodrax(), new BoneyardMycodrax()));
        Permanent mycodrax = addCreatureReady(player1, new BoneyardMycodrax());

        assertThat(gqs.getEffectivePower(gd, mycodrax)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mycodrax)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new BoneyardMycodrax(), new BoneyardMycodrax()));

        assertThat(gqs.getEffectivePower(gd, mycodrax)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mycodrax)).isEqualTo(2);

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, mycodrax)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, mycodrax)).isZero();
    }

    @Test
    void graveyardPowerAndToughnessExcludeTheCardItself() {
        BoneyardMycodrax mycodrax = new BoneyardMycodrax();
        harness.setGraveyard(player1, List.of(mycodrax));
        harness.setGraveyard(player2, List.of(new BoneyardMycodrax()));

        assertThat(gqs.getEffectiveCardPower(gd, mycodrax)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, mycodrax)).isZero();

        harness.setGraveyard(player1, List.of(mycodrax, new BoneyardMycodrax()));

        assertThat(gqs.getEffectiveCardPower(gd, mycodrax)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, mycodrax)).isEqualTo(1);
    }

    @Test
    void scavengeWithNoOtherCreatureCardsPutsNoCountersOnOpposingCreature() {
        harness.setGraveyard(player1, List.of(new BoneyardMycodrax()));
        harness.setGraveyard(player2, List.of(new BoneyardMycodrax(), new BoneyardMycodrax()));
        Permanent target = addCreatureReady(player2, new BoneyardMycodrax());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.assertNotInGraveyard(player1, "Boneyard Mycodrax");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Boneyard Mycodrax");
    }

    @Test
    void scavengeUsesPowerBeforeExileDespiteAnotherCreatureDyingInResponse() {
        harness.setGraveyard(player1, List.of(new BoneyardMycodrax(), new BoneyardMycodrax()));
        Permanent target = addCreatureReady(player1, new BoneyardMycodrax());
        Permanent other = addCreatureReady(player1, new BoneyardMycodrax());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, other.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void scavengeCannotBeActivatedOutsideMainPhase() {
        harness.setGraveyard(player1, List.of(new BoneyardMycodrax(), new BoneyardMycodrax()));
        Permanent target = addCreatureReady(player1, new BoneyardMycodrax());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scavengeCannotBeActivatedWithANonemptyStack() {
        harness.setGraveyard(player1, List.of(new BoneyardMycodrax(), new BoneyardMycodrax()));
        Permanent target = addCreatureReady(player1, new BoneyardMycodrax());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromHand(player1, new BoneyardMycodrax(), "{2}{B}");

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
    }
}
