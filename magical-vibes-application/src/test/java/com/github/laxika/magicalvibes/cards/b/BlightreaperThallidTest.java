package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
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

@CardUsed({BlightreaperThallid.class, BlightsowerThallid.class, FlameJavelin.class})
class BlightreaperThallidTest extends BaseCardTest {

    @Test
    void transformsAndCreatesPhyrexianSaproling() {
        Permanent thallid = addThallid();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        transform(thallid);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castInstant(player2, 0, thallid.getId());
        harness.passBothPriorities();
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

    private void transform(Permanent thallid) {
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
