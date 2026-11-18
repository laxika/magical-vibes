package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HamperingSnare.class, GrizzlyBears.class, HumbleNaturalist.class})
class HamperingSnareTest extends BaseCardTest {

    private void castSnare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new HamperingSnare(), "{1}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gives opponents' creatures -2/-0 until end of turn")
    void affectsOnlyOpponentsCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSnare();

        assertThat(gqs.getEffectivePower(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mine)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(2);
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void wearsOff() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSnare();
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new HamperingSnare()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hampering Snare");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectLaterCreatures() {
        Permanent existing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSnare();
        Permanent later = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existing)).isZero();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reduces every opposing creature and permits negative power")
    void affectsMultipleCreaturesAndAllowsNegativePower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent naturalist = harness.addToBattlefieldAndReturn(player2, new HumbleNaturalist());

        castSnare();

        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectivePower(gd, naturalist)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, naturalist)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Humble Naturalist");
    }

    @Test
    @DisplayName("Cycling pays its discard cost immediately and does not reduce power")
    void cyclingDoesNotApplySpellEffect() {
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HamperingSnare()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Hampering Snare");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(2);
    }
}
