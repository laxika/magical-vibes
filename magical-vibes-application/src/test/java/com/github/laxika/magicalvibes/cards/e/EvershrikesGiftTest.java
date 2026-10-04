package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({EvershrikesGift.class, GrizzlyBears.class, HillGiant.class})
class EvershrikesGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Evershrike's Gift attaches it and grants +1/+0 and flying")
    void resolvingAttachesAndBoosts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EvershrikesGift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent gift = findPermanent(player1, "Evershrike's Gift");
        assertThat(gift.isAttached()).isTrue();
        assertThat(gift.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Blight 2 returns Evershrike's Gift from the graveyard to its owner's hand")
    void blightReturnsGiftToHand() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Evershrike's Gift");
        harness.assertInHand(player1, "Evershrike's Gift");
    }

    @Test
    @DisplayName("Blight 2 prompts for the creature when more than one creature is controlled")
    void blightPromptsForCreatureChoice() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertInHand(player1, "Evershrike's Gift");
    }

    @Test
    @DisplayName("Blight 2 can only be activated at sorcery speed")
    void blightIsSorcerySpeed() {
        addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Evershrike's Gift can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new EvershrikesGift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Evershrike's Gift").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Blight can kill the chosen creature without preventing the return")
    void lethalBlightStillReturnsGift() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Evershrike's Gift");
        harness.assertInHand(player1, "Evershrike's Gift");
    }

    @Test
    @DisplayName("Only the activated copy returns from the graveyard")
    void returnsOnlyActivatedCopy() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        EvershrikesGift activated = new EvershrikesGift();
        EvershrikesGift other = new EvershrikesGift();
        harness.setGraveyard(player1, List.of(activated, other));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(activated).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the blight cost")
    void cannotActivateWithoutControlledCreature() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Evershrike's Gift");
        harness.assertNotInHand(player1, "Evershrike's Gift");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana prevents activation before any counters are paid")
    void cannotActivateWithoutEnoughMana() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Evershrike's Gift");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated outside a main phase")
    void cannotActivateDuringUpkeep() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Evershrike's Gift");
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated while an Aura spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new EvershrikesGift()));
        harness.setGraveyard(player1, List.of(new EvershrikesGift()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, giant.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Evershrike's Gift");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }
}
