package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CatCollector;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsatiableAppetite.class, CatCollector.class, FountainOfYouth.class, GrizzlyBears.class,
        Gingerbrute.class, GoldenEgg.class})
class InsatiableAppetiteTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Food gives the target creature +5/+5")
    void sacrificingFoodGivesLargerBoost() {
        addFood();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castAppetite(bears.getId());
        Permanent food = findPermanent(player1, "Food");
        harness.handleMultiplePermanentsChosen(player1, List.of(food.getId()));

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(bears.getEffectivePower()).isEqualTo(7);
        assertThat(bears.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Declining to sacrifice a Food gives the target creature +3/+3")
    void decliningFoodGivesSmallerBoost() {
        addFood();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castAppetite(bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Without a Food, the target creature gets +3/+3")
    void noFoodGivesSmallerBoostAutomatically() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castAppetite(bears.getId());

        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new InsatiableAppetite()));
        addManaForAppetite();

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canSacrificeNontokenFoodToBoostOpponentsCreature() {
        Permanent food = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        Permanent target = addCreatureReady(player2, new Gingerbrute());

        castAppetite(target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(food.getId()));

        assertThat(countPermanents(player1, "Golden Egg")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(food.getCard());
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void opponentsFoodDoesNotEnableLargerBoost() {
        harness.addToBattlefield(player2, new GoldenEgg());
        Permanent target = addCreatureReady(player1, new Gingerbrute());

        castAppetite(target.getId());

        assertThat(countPermanents(player2, "Golden Egg")).isOne();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void canSacrificeTargetWhenItIsFood() {
        Permanent target = addCreatureReady(player1, new Gingerbrute());

        castAppetite(target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(countPermanents(player1, "Gingerbrute")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void illegalTargetPreventsFoodSacrificeChoice() {
        Permanent target = addCreatureReady(player1, new Gingerbrute());
        harness.addToBattlefield(player1, new GoldenEgg());
        harness.setHand(player1, List.of(new InsatiableAppetite()));
        addManaForAppetite();
        harness.castInstant(player1, 0, target.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Gingerbrute")).isZero();
        assertThat(countPermanents(player1, "Golden Egg")).isOne();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addFood() {
        harness.setHand(player1, List.of(new CatCollector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void castAppetite(UUID targetId) {
        harness.setHand(player1, List.of(new InsatiableAppetite()));
        addManaForAppetite();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addManaForAppetite() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
