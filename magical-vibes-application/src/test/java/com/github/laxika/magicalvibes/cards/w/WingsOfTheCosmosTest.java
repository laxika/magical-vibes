package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WingsOfTheCosmos.class, FearlessPup.class, GoldveinPick.class, FrostBite.class})
class WingsOfTheCosmosTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts, grants flying to, and untaps the target creature")
    void resolvesAllEffects() {
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        harness.setHand(player1, List.of(new WingsOfTheCosmos()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        pup.tap();
        UUID targetId = pup.getId();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(pup.getPowerModifier()).isEqualTo(1);
        assertThat(pup.getToughnessModifier()).isEqualTo(3);
        assertThat(pup.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(pup.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost and flying wear off at end of turn")
    void temporaryEffectsWearOff() {
        Permanent pup = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        harness.setHand(player1, List.of(new WingsOfTheCosmos()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Fearless Pup");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(pup.getPowerModifier()).isZero();
        assertThat(pup.getToughnessModifier()).isZero();
        assertThat(pup.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GoldveinPick());
        harness.setHand(player1, List.of(new WingsOfTheCosmos()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Goldvein Pick");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost and untap an opponent's creature without affecting another creature")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        target.tap();
        other.tap();
        harness.setHand(player1, List.of(new WingsOfTheCosmos()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped creature still receives the boost and flying")
    void canTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        harness.setHand(player1, List.of(new WingsOfTheCosmos()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not resolve any effects when its target dies in response")
    void targetDiesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        target.tap();
        harness.setHand(player1, List.of(new WingsOfTheCosmos()));
        harness.setHand(player2, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Fearless Pup");
        harness.assertInGraveyard(player1, "Fearless Pup");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wings of the Cosmos");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
