package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoblinOriflamme;
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

@CardUsed({AshrootAnimist.class, BearCub.class, GiantGrowth.class, GoblinOriflamme.class})
class AshrootAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another creature you control")
    void attackTriggerTargetsAnotherOwnCreature() {
        Permanent animist = addCreatureReady(player1, new AshrootAnimist());
        Permanent ownCreature = addCreatureReady(player1, new BearCub());
        Permanent opposingCreature = addCreatureReady(player2, new BearCub());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .contains(ownCreature.getId())
                .doesNotContain(animist.getId(), opposingCreature.getId());
    }

    @Test
    @DisplayName("Attacking gives the target trample and +X/+X where X is Ashroot Animist's power")
    void attackBoostsAndGrantsTrampleUsingCurrentPower() {
        Permanent animist = addCreatureReady(player1, new AshrootAnimist());
        Permanent target = addCreatureReady(player1, new BearCub());
        animist.setPowerModifier(2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The attack boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new AshrootAnimist());
        Permanent target = addCreatureReady(player1, new BearCub());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void usesPowerAtResolutionAndLocksInTheBoost() {
        Permanent animist = addCreatureReady(player1, new AshrootAnimist());
        Permanent target = addCreatureReady(player1, new BearCub());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player1, 0, animist.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        animist.setPowerModifier(0);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
    }

    @Test
    void usesLastKnownPowerWhenSourceLeavesBeforeResolution() {
        Permanent animist = addCreatureReady(player1, new AshrootAnimist());
        Permanent target = addCreatureReady(player1, new BearCub());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        animist.setPowerModifier(2);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, animist);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void lastKnownPowerIncludesStaticBonusesBeforeSourceLeaves() {
        Permanent animist = addCreatureReady(player1, new AshrootAnimist());
        Permanent target = addCreatureReady(player1, new BearCub());
        harness.addToBattlefield(player1, new GoblinOriflamme());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gqs.getEffectivePower(gd, animist)).isEqualTo(5);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, animist);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void doesNotAffectTargetThatChangesControllerBeforeResolution() {
        addCreatureReady(player1, new AshrootAnimist());
        Permanent target = addCreatureReady(player1, new BearCub());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void canTargetAnotherAshrootAnimist() {
        Permanent source = addCreatureReady(player1, new AshrootAnimist());
        Permanent target = addCreatureReady(player1, new AshrootAnimist());

        declareAttackers(List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(source.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
    }

    @Test
    void attackingAloneDoesNotBoostItself() {
        Permanent animist = addCreatureReady(player1, new AshrootAnimist());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, animist)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, animist)).isEqualTo(4);
    }
}
