package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PheresBandThunderhoof;
import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrengthFromTheFallen.class, GoldenHind.class, PheresBandThunderhoof.class, FontOfFertility.class})
class StrengthFromTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry boosts the chosen creature by the number of creature cards in its graveyard")
    void ownEntryBoostsTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setGraveyard(player1, List.of(new GoldenHind(), new PheresBandThunderhoof(), new FontOfFertility()));
        harness.setHand(player1, List.of(new StrengthFromTheFallen()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent target = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers the boost")
    void allyEnchantmentEntryBoostsTarget() {
        harness.addToBattlefield(player1, new StrengthFromTheFallen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setGraveyard(player1, List.of(new GoldenHind(), new PheresBandThunderhoof()));
        harness.setHand(player1, List.of(new FontOfFertility()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new GoldenHind());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new FontOfFertility());
        harness.setHand(player1, List.of(new StrengthFromTheFallen()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsControllersGraveyardAtResolutionWhenTargetingOpponentCreature() {
        harness.addToBattlefield(player1, new StrengthFromTheFallen());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setGraveyard(player1, List.of(new GoldenHind()));
        harness.setGraveyard(player2, List.of(new GoldenHind(), new GoldenHind(), new GoldenHind()));
        harness.setHand(player1, List.of(new FontOfFertility()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int power = gqs.getEffectivePower(gd, target);
        int toughness = gqs.getEffectiveToughness(gd, target);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new GoldenHind(), new PheresBandThunderhoof(), new FontOfFertility()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness + 2);
        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power + 2);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power + 2);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness);
    }

    @Test
    void emptyGraveyardStillTriggersButGivesNoBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setGraveyard(player1, List.of(new FontOfFertility()));
        harness.setHand(player1, List.of(new StrengthFromTheFallen()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        int power = gqs.getEffectivePower(gd, target);
        int toughness = gqs.getEffectiveToughness(gd, target);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness);
    }

    @Test
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new StrengthFromTheFallen());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setGraveyard(player1, List.of(new GoldenHind()));
        harness.setHand(player2, List.of(new FontOfFertility()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int power = gqs.getEffectivePower(gd, target);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power);
    }

    @Test
    void triggerStillResolvesAfterSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new StrengthFromTheFallen());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setGraveyard(player1, List.of(new GoldenHind()));
        harness.setHand(player1, List.of(new FontOfFertility()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int power = gqs.getEffectivePower(gd, target);
        int toughness = gqs.getEffectiveToughness(gd, target);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(power + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughness + 1);
    }

    @Test
    void canBeCastWithNoCreaturesOnBattlefield() {
        harness.setHand(player1, List.of(new StrengthFromTheFallen()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Strength from the Fallen");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
