package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrenkosCommand;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({RapidAugmenter.class, LlanowarElves.class, GrizzlyBears.class, KrenkosCommand.class})
class RapidAugmenterTest extends BaseCardTest {

    @Test
    @DisplayName("A cast creature with base power 1 gains haste but does not augment Rapid Augmenter")
    void castBasePowerOneCreatureGainsHasteOnly() {
        Permanent augmenter = harness.addToBattlefieldAndReturn(player1, new RapidAugmenter());
        setUpMainPhase();
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent elves = findPermanent(player1, "Llanowar Elves");
        assertThat(elves.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(augmenter.getEffectivePower()).isEqualTo(1);
        assertThat(augmenter.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A creature with base power other than 1 does not gain haste")
    void otherBasePowerDoesNotGainHaste() {
        Permanent augmenter = harness.addToBattlefieldAndReturn(player1, new RapidAugmenter());
        setUpMainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(augmenter.getEffectivePower()).isEqualTo(1);
        assertThat(augmenter.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Uncast creature tokens put counters on Rapid Augmenter and make it unblockable")
    void uncastTokensAugmentAndMakeItUnblockable() {
        Permanent augmenter = harness.addToBattlefieldAndReturn(player1, new RapidAugmenter());
        setUpMainPhase();
        harness.setHand(player1, List.of(new KrenkosCommand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Goblin");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> assertThat(token.hasKeyword(Keyword.HASTE)).isTrue());
        assertThat(augmenter.getEffectivePower()).isEqualTo(3);
        assertThat(augmenter.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(augmenter.getEffectivePower()).isEqualTo(3);
        assertThat(augmenter.isCantBeBlocked()).isFalse();
        assertThat(tokens).allSatisfy(token -> assertThat(token.hasKeyword(Keyword.HASTE)).isFalse());
    }

    private void setUpMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
