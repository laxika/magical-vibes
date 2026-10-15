package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistfireWeaver.class, GrizzlyBears.class, ForceAway.class})
class MistfireWeaverTest extends BaseCardTest {

    @Test
    void turningFaceUpGivesTargetCreatureYouControlHexproofUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MistfireWeaver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Mistfire Weaver");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void canTargetItselfAndGrantLastsThroughEndStep() {
        harness.setHand(player1, List.of(new MistfireWeaver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Mistfire Weaver");
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.HEXPROOF)).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(weaver.getId());
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.HEXPROOF)).isFalse();
        harness.handlePermanentChosen(player1, weaver.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.HEXPROOF)).isTrue();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void castingFaceUpDoesNotTriggerHexproofGrant() {
        harness.castFromHand(player1, new MistfireWeaver(), "{3}{U}");
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Mistfire Weaver");
        assertThat(gqs.hasKeyword(gd, weaver, Keyword.HEXPROOF)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillResolvesAfterWeaverLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MistfireWeaver());
        harness.setHand(player1, List.of(new MistfireWeaver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source));
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new ForceAway()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }
}
