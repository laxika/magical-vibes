package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrazingGladehart;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({WindborneCharge.class, GrazingGladehart.class, Mountain.class, IntoTheRoil.class})
class WindborneChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives two creatures you control +2/+2 and flying")
    void boostsTwoOwnCreaturesAndGrantsFlying() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        harness.setHand(player1, List.of(new WindborneCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(first.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        harness.setHand(player1, List.of(new WindborneCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(2);
        assertThat(first.getEffectiveToughness()).isEqualTo(2);
        assertThat(first.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(second.getEffectivePower()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent creature or a noncreature permanent")
    void targetsMustBeOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrazingGladehart());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new WindborneCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownCreature.getId(), ownLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires exactly two distinct targets")
    void requiresExactlyTwoDistinctTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        harness.setHand(player1, List.of(new WindborneCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still boosts the remaining target when the first target leaves")
    void resolvesForRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        harness.setHand(player1, List.of(new WindborneCharge()));
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grazing Gladehart");
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(untargeted.getEffectivePower()).isEqualTo(2);
        assertThat(untargeted.getEffectiveToughness()).isEqualTo(2);
        assertThat(untargeted.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not affect other creatures when both targets leave")
    void doesNotResolveWhenBothTargetsLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new GrazingGladehart());
        harness.setHand(player1, List.of(new WindborneCharge()));
        harness.setHand(player2, List.of(new IntoTheRoil(), new IntoTheRoil()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.castAndResolveInstant(player2, 0, second.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Windborne Charge");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(untargeted.getEffectivePower()).isEqualTo(2);
        assertThat(untargeted.getEffectiveToughness()).isEqualTo(2);
        assertThat(untargeted.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
