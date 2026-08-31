package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FyndhornDruid;
import com.github.laxika.magicalvibes.cards.l.LakeOfTheDead;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SurgeOfStrength.class, FyndhornDruid.class, StormShaman.class, LakeOfTheDead.class})
class SurgeOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a green card grants trample and +X/+0 equal to the target's mana value")
    void grantsTrampleAndManaValueBoost() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new FyndhornDruid());
        harness.setHand(player1, List.of(new SurgeOfStrength(), new FyndhornDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithDiscard(player1, 0, druid.getId(), 1);
        harness.passBothPriorities();

        Permanent resolvedDruid = findPermanent(player1, "Fyndhorn Druid");
        assertThat(resolvedDruid.getEffectivePower()).isEqualTo(5);
        assertThat(resolvedDruid.getEffectiveToughness()).isEqualTo(2);
        assertThat(resolvedDruid.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Fyndhorn Druid");
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new FyndhornDruid());
        harness.setHand(player1, List.of(new SurgeOfStrength(), new FyndhornDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithDiscard(player1, 0, druid.getId(), 1);
        harness.passBothPriorities();

        assertThat(druid.getEffectivePower()).isEqualTo(5);
        assertThat(druid.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Discarding a red card also pays the additional cost")
    void acceptsRedCardAsAdditionalCost() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new FyndhornDruid());
        harness.setHand(player1, List.of(new SurgeOfStrength(), new StormShaman()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithDiscard(player1, 0, druid.getId(), 1);
        harness.passBothPriorities();

        Permanent resolvedDruid = findPermanent(player1, "Fyndhorn Druid");
        assertThat(resolvedDruid.getEffectivePower()).isEqualTo(5);
        assertThat(resolvedDruid.hasKeyword(Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Storm Shaman");
    }

    @Test
    @DisplayName("Trample and the boost wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new FyndhornDruid());
        harness.setHand(player1, List.of(new SurgeOfStrength(), new FyndhornDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithDiscard(player1, 0, druid.getId(), 1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent resolvedDruid = findPermanent(player1, "Fyndhorn Druid");
        assertThat(resolvedDruid.getPowerModifier()).isEqualTo(0);
        assertThat(resolvedDruid.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot be cast without a red or green card to discard")
    void cannotCastWithoutRedOrGreenCard() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new FyndhornDruid());
        harness.setHand(player1, List.of(new SurgeOfStrength(), new LakeOfTheDead()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, druid.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a player as target")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new SurgeOfStrength(), new FyndhornDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature permanent as target")
    void rejectsNoncreaturePermanentTarget() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new LakeOfTheDead());
        harness.setHand(player1, List.of(new SurgeOfStrength(), new FyndhornDruid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, lake.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
