package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DeadlyDerision;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightreaperThallid.class, BlightsowerThallid.class, DeadlyDerision.class})
class BlightreaperThallidTest extends BaseCardTest {

    @Test
    void transformsAndCreatesPhyrexianSaproling() {
        Permanent thallid = addThallid();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(thallid.isTransformed()).isTrue();
        assertThat(thallid.getCard()).isInstanceOf(BlightsowerThallid.class);
        Permanent token = findPermanent(player1, "Phyrexian Saproling");
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.PHYREXIAN, CardSubtype.SAPROLING);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void phyrexianManaCanBePaidWithLife() {
        Permanent thallid = addThallid();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(thallid.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void transformedThallidCreatesPhyrexianSaprolingWhenItDies() {
        Permanent thallid = addThallid();
        transform();

        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new DeadlyDerision()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player2, 0, thallid.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(thallid.getId()));
        assertThat(findPermanents(player1, "Phyrexian Saproling")).hasSize(2);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addThallid();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addThallid() {
        return harness.addToBattlefieldAndReturn(player1, new BlightreaperThallid());
    }

    @Test
    void frontFaceDeathDoesNotCreateSaproling() {
        Permanent thallid = addThallid();
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new DeadlyDerision()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, thallid.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Blightreaper Thallid");
        assertThat(findPermanents(player1, "Phyrexian Saproling")).isEmpty();
    }

    @Test
    void removalInResponsePreventsTransformationAndBothTokenTriggers() {
        Permanent thallid = addThallid();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new DeadlyDerision()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, thallid.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Blightreaper Thallid");
        assertThat(findPermanents(player1, "Phyrexian Saproling")).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void tappedSummoningSickThallidCanTransformInPostcombatMainPhase() {
        Permanent thallid = addThallid();
        thallid.tap();
        thallid.setSummoningSick(true);
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(thallid.isTransformed()).isTrue();
        assertThat(thallid.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Phyrexian Saproling")).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotTransformDuringOwnUpkeep() {
        addThallid();
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotTransformWhileStackIsNotEmpty() {
        Permanent thallid = addThallid();
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new DeadlyDerision()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, thallid.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    private void transform() {
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
