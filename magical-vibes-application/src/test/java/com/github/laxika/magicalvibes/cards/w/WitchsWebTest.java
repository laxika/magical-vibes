package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.b.BloodFountain;
import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
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

@CardUsed({WitchsWeb.class, SnarlingWolf.class, BloodFountain.class, Abrade.class})
class WitchsWebTest extends BaseCardTest {

    @Test
    @DisplayName("Witch's Web untaps a creature and gives it +3/+3 and reach")
    void untapsAndBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnarlingWolf());
        target.tap();
        harness.setHand(player1, List.of(new WitchsWeb()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Witch's Web's boost and reach expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnarlingWolf());
        target.tap();
        harness.setHand(player1, List.of(new WitchsWeb()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Witch's Web cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BloodFountain());
        harness.setHand(player1, List.of(new WitchsWeb()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Witch's Web can target an untapped creature you control without affecting other creatures")
    void boostsUntappedCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SnarlingWolf());
        other.tap();
        harness.setHand(player1, List.of(new WitchsWeb()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.REACH)).isFalse();
        harness.assertInGraveyard(player1, "Witch's Web");
    }

    @Test
    @DisplayName("Witch's Web does not resolve if its target dies in response")
    void doesNotResolveWhenTargetDiesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        target.tap();
        harness.setHand(player1, List.of(new WitchsWeb()));
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertNotOnBattlefield(player1, "Snarling Wolf");
        harness.assertInGraveyard(player1, "Snarling Wolf");
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Witch's Web");
    }
}
