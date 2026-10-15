package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
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

@CardUsed({IronhoofBoar.class, JukaiTrainee.class, Forest.class})
class IronhoofBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Channel boosts a creature, grants trample, and discards Ironhoof Boar")
    void channelBoostsTargetCreatureAndDiscardsSource() {
        harness.setHand(player1, List.of(new IronhoofBoar()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Ironhoof Boar");
    }

    @Test
    @DisplayName("Channel's boost and trample wear off at end of turn")
    void channelEffectsWearOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new IronhoofBoar()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Channel cannot target a noncreature permanent")
    void channelRejectsNoncreatureTarget() {
        harness.setHand(player1, List.of(new IronhoofBoar()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ironhoof Boar");
    }

    @Test
    @DisplayName("Channel can target an opponent's creature and discards before resolving")
    void channelCanBoostOpponentsCreature() {
        harness.setHand(player1, List.of(new IronhoofBoar()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Ironhoof Boar");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Channel requires red mana and does not discard when its cost cannot be paid")
    void channelRejectsManaWithoutRed() {
        harness.setHand(player1, List.of(new IronhoofBoar()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ironhoof Boar");
        harness.assertNotInGraveyard(player1, "Ironhoof Boar");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }
}
