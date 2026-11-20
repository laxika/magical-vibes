package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CatCollector;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
    }

    @Test
    @DisplayName("Declining to sacrifice a Food gives the target creature +3/+3")
    void decliningFoodGivesSmallerBoost() {
        addFood();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castAppetite(bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Without a Food, the target creature gets +3/+3")
    void noFoodGivesSmallerBoostAutomatically() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castAppetite(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
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
    @DisplayName("A nontoken Food can be sacrificed to give an opponent's creature +5/+5 until end of turn")
    void canSacrificeNontokenFoodToBoostOpponentsCreature() {
        Permanent food = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        Permanent target = addCreatureReady(player2, new Gingerbrute());

        castAppetite(target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Golden Egg")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(food.getCard());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Food doesn't let the creature get +5/+5")
    void opponentsFoodDoesNotEnableLargerBoost() {
        harness.addToBattlefield(player2, new GoldenEgg());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castAppetite(target.getId());

        assertThat(countPermanents(player2, "Golden Egg")).isOne();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The target can itself be the Food sacrificed")
    void canSacrificeTargetWhenItIsFood() {
        Permanent target = addCreatureReady(player1, new Gingerbrute());

        castAppetite(target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Gingerbrute")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An illegal target means the spell doesn't resolve and no Food is sacrificed")
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

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Sacrificing a Food creature asks for its death replacement before it is sacrificed or the boost applies")
    void asksForTheDeathReplacementBeforeTheFoodCreatureIsSacrificed() {
        Permanent gingerbrute = addCreatureReady(player1, new Gingerbrute());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(gingerbrute.getId());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castAppetite(bears.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.inDepartureBatch()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gingerbrute);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player1.getId())).contains(gingerbrute.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gingerbrute);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gingerbrute"))
                .hasSize(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

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
