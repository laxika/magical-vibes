package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.g.GeodeGrotto;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DowsingDevice.class, GeodeGrotto.class, DarksteelRelic.class, GrizzlyBears.class,
        Ornithopter.class, Abrade.class})
class DowsingDeviceTest extends BaseCardTest {

    @Test
    @DisplayName("Its entry boosts a creature, grants haste, and transforms with four artifacts")
    void entryBoostsAndTransformsAtFourArtifacts() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addArtifacts(3);
        harness.setHand(player1, List.of(new DowsingDevice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        Permanent device = findPermanent(player1, "Geode Grotto");
        assertThat(device.isTransformed()).isTrue();
        assertThat(device.getCard()).isInstanceOf(GeodeGrotto.class);
    }

    @Test
    @DisplayName("Another artifact entry boosts a chosen creature without transforming below four artifacts")
    void anotherArtifactEntryBoostsWithoutTransformingBelowThreshold() {
        harness.addToBattlefield(player1, new DowsingDevice());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addArtifacts(1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(findPermanent(player1, "Dowsing Device").isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Geode Grotto gives a target creature power equal to the artifact count and haste")
    void backFaceBoostsByArtifactCount() {
        Permanent grotto = addTransformedDevice();
        addArtifacts(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(grotto), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Geode Grotto taps for red mana")
    void backFaceTapsForRedMana() {
        Permanent grotto = addTransformedDevice();

        harness.activateAbility(player1, battlefieldIndex(grotto), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Temporary entry bonuses wear off at end of turn")
    void entryBonusesWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DowsingDevice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void artifactEntryMustNotTransformWhenItsChosenCreatureBecomesIllegal() {
        Permanent device = harness.addToBattlefieldAndReturn(player1, new DowsingDevice());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addArtifacts(2);
        harness.setHand(player1, List.of(new DarksteelRelic()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        while (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, target.getId());
        }

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(device.isTransformed()).isFalse();
    }

    @Test
    void entryTransformsWithNoCreaturesToTarget() {
        addArtifacts(3);
        harness.setHand(player1, List.of(new DowsingDevice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Geode Grotto");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void entryCanDeclineTargetingAndStillTransform() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addArtifacts(3);
        harness.setHand(player1, List.of(new DowsingDevice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Geode Grotto");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void opponentsArtifactDoesNotTriggerDevice() {
        harness.addToBattlefield(player1, new DowsingDevice());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new DarksteelRelic()));
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void backFaceCountsArtifactsAtResolutionAndBonusExpires() {
        Permanent grotto = addTransformedDevice();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        addArtifacts(2);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, battlefieldIndex(grotto), 1, null, target.getId());

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, 1, artifact.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Ornithopter");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void backFaceGrantsHasteWithZeroArtifacts() {
        Permanent grotto = addTransformedDevice();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(grotto), 1, null, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(grotto.isTapped()).isTrue();
    }

    @Test
    void backFaceBoostCannotBeActivatedOutsideMainPhase() {
        Permanent grotto = addTransformedDevice();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(grotto), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(grotto.isTapped()).isFalse();
    }

    private void addArtifacts(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new DarksteelRelic());
        }
    }

    private Permanent addTransformedDevice() {
        DowsingDevice card = new DowsingDevice();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
