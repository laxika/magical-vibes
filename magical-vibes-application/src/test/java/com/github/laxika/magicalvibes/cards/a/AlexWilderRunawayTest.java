package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlexWilderRunaway.class, GrizzlyBears.class})
class AlexWilderRunawayTest extends BaseCardTest {

    @Test
    void escapedAlexGetsBoostAndHaste() {
        AlexWilderRunaway alex = new AlexWilderRunaway();
        harness.setGraveyard(player1, List.of(
                alex, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        resolveAllTriggers();

        Permanent escapedAlex = findPermanent(player1, "Alex Wilder, Runaway");
        assertThat(escapedAlex.isEscaped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, escapedAlex)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, escapedAlex)).isEqualTo(3);
        assertThat(escapedAlex.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void creatureCastFromHandDoesNotGetBoosted() {
        harness.addToBattlefield(player1, new AlexWilderRunaway());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void AlexCastFromHandDoesNotBoostItself() {
        harness.setHand(player1, List.of(new AlexWilderRunaway()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent alex = findPermanent(player1, "Alex Wilder, Runaway");
        assertThat(gqs.getEffectivePower(gd, alex)).isEqualTo(1);
        assertThat(alex.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void creaturePutOntoBattlefieldWithoutCastingDoesNotGetBoosted() {
        harness.addToBattlefield(player1, new AlexWilderRunaway());

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void AlexPutOntoBattlefieldWithoutCastingDoesNotBoostItself() {
        Permanent alex = harness.enterBattlefieldAndReturn(player1, new AlexWilderRunaway());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, alex)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, alex)).isEqualTo(3);
        assertThat(alex.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void escapedAlexBoostAndHasteExpireAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(
                new AlexWilderRunaway(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        resolveAllTriggers();

        Permanent alex = findPermanent(player1, "Alex Wilder, Runaway");
        assertThat(gqs.getEffectivePower(gd, alex)).isEqualTo(3);
        assertThat(alex.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, alex)).isEqualTo(1);
        assertThat(alex.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void escapeCannotExileAlexItselfAsOneOfTheThreeCards() {
        harness.setGraveyard(player1, List.of(
                new AlexWilderRunaway(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
