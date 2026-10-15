package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TatyovaStewardOfTides.class, Forest.class})
class TatyovaStewardOfTidesTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall animates up to one controlled land once seven lands are controlled")
    void landfallAnimatesControlledLandAtSevenLands() {
        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId(), player1.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Landfall does not trigger before controlling seven lands")
    void landfallDoesNotTriggerBelowSevenLands() {
        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Landfall only permits controlled lands and may be declined")
    void landfallRestrictsTargetsAndMayBeDeclined() {
        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gqs.isCreature(gd, ownLand)).isFalse();
    }

    @Test
    void seventhLandCanTargetItselfAndAnimationSurvivesCleanup() {
        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent enteringLand = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.handlePermanentChosen(player1, enteringLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, enteringLand)).isTrue();
        assertThat(gqs.hasKeyword(gd, enteringLand, Keyword.HASTE)).isTrue();
        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(gqs.isLand(gd, enteringLand)).isTrue();
        assertThat(gqs.isCreature(gd, enteringLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enteringLand)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enteringLand)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enteringLand, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enteringLand, Keyword.FLYING)).isTrue();
    }

    @Test
    void landCountIsCheckedAgainOnResolution() {
        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent removedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, removedLand));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void triggerResolvesWithoutTatyovaAndAnimationRemainsWithoutFlying() {
        Permanent tatyova = harness.addToBattlefieldAndReturn(player1, new TatyovaStewardOfTides());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, tatyova));
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new TatyovaStewardOfTides());
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, gd.playerBattlefields.get(player1.getId()).getLast(), Keyword.FLYING))
                .isFalse();
    }

    @Test
    @CardUsed({TatyovaStewardOfTides.class, AshayaSoulOfTheWild.class})
    void tatyovaAlsoHasFlyingWhenSheIsALandCreature() {
        Permanent tatyova = harness.addToBattlefieldAndReturn(player1, new TatyovaStewardOfTides());
        Permanent ashaya = harness.addToBattlefieldAndReturn(player1, new AshayaSoulOfTheWild());

        assertThat(gqs.isLand(gd, tatyova)).isTrue();
        assertThat(gqs.isCreature(gd, tatyova)).isTrue();
        assertThat(gqs.hasKeyword(gd, ashaya, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, tatyova, Keyword.FLYING)).isTrue();
    }
}
