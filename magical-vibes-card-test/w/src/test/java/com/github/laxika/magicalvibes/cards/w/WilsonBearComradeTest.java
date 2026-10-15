package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CastDown;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.o.Owlbear;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({WilsonBearComrade.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Swamp.class, CastDown.class, Owlbear.class})
class WilsonBearComradeTest extends BaseCardTest {

    @Test
    void baseFaceHasReachTrampleAndWard() {
        Permanent wilson = addCreatureReady(player1, new WilsonBearComrade());

        assertThat(gqs.hasKeyword(gd, wilson, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.WARD)).isTrue();
    }

    @Test
    void whiteFaceHasLifelinkAndCorrectPowerToughness() {
        Permanent wilson = specialize(CardColor.WHITE, 0, new Plains());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Urbane Bear");
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(4);
    }

    @Test
    void blueFaceCannotBeBlocked() {
        Permanent wilson = specialize(CardColor.BLUE, 1, new Island());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Subtle Bear");
        assertThat(gqs.hasCantBeBlocked(gd, wilson)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(3);
    }

    @Test
    void blackFaceHasMenace() {
        Permanent wilson = specialize(CardColor.BLACK, 2, new Swamp());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Fearsome Bear");
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(4);
    }

    @Test
    void redFaceHasDoubleStrike() {
        Permanent wilson = specialize(CardColor.RED, 3, new Mountain());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Ardent Bear");
        assertThat(gqs.hasKeyword(gd, wilson, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(3);
    }

    @Test
    void greenFaceHasFiveFiveBody() {
        Permanent wilson = specialize(CardColor.GREEN, 4, new Forest());

        assertThat(wilson.getCard().getName()).isEqualTo("Wilson, Majestic Bear");
        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wilson)).isEqualTo(5);
    }

    @Test
    void greenGraveyardAbilityPerpetuallyBoostsAndGrantsKeywords() {
        Permanent wilson = specialize(CardColor.GREEN, 4, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wilson));

        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        int wilsonGraveyardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(wilson.getCard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateGraveyardAbility(player1, wilsonGraveyardIndex, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.WARD)).isTrue();
    }

    @Test
    void whiteGraveyardAbilityGrantsLifelink() {
        Card wilson = specializeIntoGraveyard(CardColor.WHITE, 0, new Plains());
        Permanent target = addCreatureReady(player1, new Owlbear());

        activateGraveyardGrant(wilson, target, ManaColor.WHITE);

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        harness.assertNotInGraveyard(player1, "Wilson, Urbane Bear");
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card().getId()).isEqualTo(wilson.getId()));
    }

    @Test
    void blueGraveyardAbilityGrantsUnblockability() {
        Card wilson = specializeIntoGraveyard(CardColor.BLUE, 1, new Island());
        Permanent target = addCreatureReady(player1, new Owlbear());

        activateGraveyardGrant(wilson, target, ManaColor.BLUE);

        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
    }

    @Test
    void blueGraveyardGrantSurvivesBounceAndRecasting() {
        Card wilson = specializeIntoGraveyard(CardColor.BLUE, 1, new Island());
        Permanent target = addCreatureReady(player1, new Owlbear());
        activateGraveyardGrant(wilson, target, ManaColor.BLUE);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasCantBeBlocked(gd, findPermanent(player1, "Owlbear"))).isTrue();
    }

    @Test
    void blackGraveyardAbilityGrantsMenace() {
        Card wilson = specializeIntoGraveyard(CardColor.BLACK, 2, new Swamp());
        Permanent target = addCreatureReady(player1, new Owlbear());

        activateGraveyardGrant(wilson, target, ManaColor.BLACK);

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    void redGraveyardAbilityGrantsDoubleStrike() {
        Card wilson = specializeIntoGraveyard(CardColor.RED, 3, new Mountain());
        Permanent target = addCreatureReady(player1, new Owlbear());

        activateGraveyardGrant(wilson, target, ManaColor.RED);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void repeatedGreenGrantsProduceIndependentWardTriggers() {
        Card first = specializeIntoGraveyard(CardColor.GREEN, 4, new Forest());
        Card second = specializeIntoGraveyard(CardColor.GREEN, 4, new Forest());
        Permanent target = addCreatureReady(player1, new Owlbear());
        activateGraveyardGrant(first, target, ManaColor.GREEN);
        activateGraveyardGrant(second, target, ManaColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CastDown()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    void specializingRetainsPerpetualPowerToughnessBoost() {
        Card donor = specializeIntoGraveyard(CardColor.GREEN, 4, new Forest());
        Permanent target = addCreatureReady(player1, new WilsonBearComrade());
        activateGraveyardGrant(donor, target, ManaColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void specializingRetainsPerpetualUnblockability() {
        Card donor = specializeIntoGraveyard(CardColor.BLUE, 1, new Island());
        Permanent target = addCreatureReady(player1, new WilsonBearComrade());
        activateGraveyardGrant(donor, target, ManaColor.BLUE);
        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasCantBeBlocked(gd, target)).isTrue();
    }

    @Test
    void graveyardGrantCannotTargetOpponentsCreature() {
        Card wilson = specializeIntoGraveyard(CardColor.WHITE, 0, new Plains());
        Permanent target = addCreatureReady(player2, new Owlbear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerGraveyards.get(player1.getId()).indexOf(wilson);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, index, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wilson);
    }

    @Test
    void specializeCannotBeActivatedOutsideMainPhase() {
        addCreatureReady(player1, new WilsonBearComrade());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 4, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void graveyardGrantCannotBeActivatedOutsideMainPhase() {
        Card wilson = specializeIntoGraveyard(CardColor.WHITE, 0, new Plains());
        Permanent target = addCreatureReady(player1, new Owlbear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int index = gd.playerGraveyards.get(player1.getId()).indexOf(wilson);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, index, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wilson);
    }

    @Test
    void specializeCanDiscardColoredNonlandCard() {
        Permanent wilson = specialize(CardColor.GREEN, 4, new WilsonBearComrade());

        assertThat(gqs.getEffectivePower(gd, wilson)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Wilson, Bear Comrade");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Card specializeIntoGraveyard(CardColor color, int abilityIndex, Card discard) {
        Permanent wilson = specialize(color, abilityIndex, discard);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wilson));
        return wilson.getCard();
    }

    private void activateGraveyardGrant(Card wilson, Permanent target, ManaColor color) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, color, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int index = gd.playerGraveyards.get(player1.getId()).indexOf(wilson);
        harness.activateGraveyardAbility(player1, index, target.getId());
        resolveAllTriggers();
    }

    private Permanent specialize(CardColor color, int abilityIndex, Card discard) {
        Permanent wilson = addCreatureReady(player1, new WilsonBearComrade());
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        return wilson;
    }
}
