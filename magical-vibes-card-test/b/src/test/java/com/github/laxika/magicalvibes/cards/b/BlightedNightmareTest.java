package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightedNightmare.class, GrizzlyBears.class, HillGiant.class})
class BlightedNightmareTest extends BaseCardTest {

    @Test
    void boostsGraveyardCreatureAndReturnsItWithBlightX() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new BlightedNightmare()));
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.perpetualPowerToughnessModifiers)
                .containsEntry(bears.getId(), new PerpetualPowerToughnessModifier(1, 1));

        Permanent nightmare = findPermanent(player1, "Blighted Nightmare");
        int nightmareIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nightmare);
        harness.activateAbility(player1, nightmareIndex, 0, 2, bears.getId(), Zone.GRAVEYARD);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Blighted Nightmare");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedBears)).isEqualTo(3);
    }

    @Test
    void cannotChooseXAboveGreatestControlledToughness() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefieldAndReturn(player1, new BlightedNightmare());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 4, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Blighted Nightmare");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void repeatedEntryBoostsStackAndOnlyAffectOwnGraveyardCreatures() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        GrizzlyBears opposingBears = new GrizzlyBears();
        BlightedNightmare graveyardEnchantment = new BlightedNightmare();
        harness.setGraveyard(player1, List.of(bears, giant, graveyardEnchantment));
        harness.setGraveyard(player2, List.of(opposingBears));
        harness.setHand(player1, List.of(new BlightedNightmare(), new BlightedNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.perpetualPowerToughnessModifiers)
                .containsEntry(bears.getId(), new PerpetualPowerToughnessModifier(2, 2))
                .containsEntry(giant.getId(), new PerpetualPowerToughnessModifier(2, 2))
                .doesNotContainKeys(opposingBears.getId(), graveyardEnchantment.getId());

        addCreatureReady(player1, new HillGiant());
        harness.activateAbility(player1, 0, 0, 2, bears.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returnedBears)).isEqualTo(4);
    }

    @Test
    void canBlightAtMaximumEvenWhenThePayingCreatureDiesAndTargetCostsLessThanX() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefieldAndReturn(player1, new BlightedNightmare());
        addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, 0, 3, bears.getId(), Zone.GRAVEYARD);
        harness.assertInHand(player1, "Blighted Nightmare");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotReturnCreatureWithManaValueGreaterThanX() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefieldAndReturn(player1, new BlightedNightmare());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Blighted Nightmare");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void cannotTargetOpponentsGraveyardOrANoncreatureCard() {
        GrizzlyBears opposingBears = new GrizzlyBears();
        BlightedNightmare graveyardEnchantment = new BlightedNightmare();
        harness.setGraveyard(player2, List.of(opposingBears));
        harness.setGraveyard(player1, List.of(graveyardEnchantment));
        harness.addToBattlefieldAndReturn(player1, new BlightedNightmare());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, opposingBears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, graveyardEnchantment.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Blighted Nightmare");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardEnchantment);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingBears);
    }

    @Test
    void cannotActivateOutsideAMainPhase() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addToBattlefieldAndReturn(player1, new BlightedNightmare());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Blighted Nightmare");
    }

    @Test
    void canChooseALowerToughnessCreatureToBlight() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefieldAndReturn(player1, new BlightedNightmare());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent payingBears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, 3, target.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, payingBears.getId());
        resolveAllTriggers();

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(payingBears.getCard());
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(target.getId());
        harness.assertInHand(player1, "Blighted Nightmare");
    }

    @Test
    void entryBoostUsesTheGraveyardWhenTheTriggerResolvesAndDoesNotBoostLaterCards() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant laterGiant = new HillGiant();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new BlightedNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(bears));
        resolveAllTriggers();
        harness.setGraveyard(player1, List.of(bears, laterGiant));

        assertThat(gd.perpetualPowerToughnessModifiers)
                .containsEntry(bears.getId(), new PerpetualPowerToughnessModifier(1, 1))
                .doesNotContainKey(laterGiant.getId());
    }
}
