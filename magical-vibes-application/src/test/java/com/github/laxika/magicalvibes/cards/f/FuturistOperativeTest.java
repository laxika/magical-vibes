package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuturistOperative.class})
class FuturistOperativeTest extends BaseCardTest {

    @Test
    void tappedOperativeBecomesAnUnblockableOneOneHumanCitizen() {
        Permanent operative = addCreatureReady(player1, new FuturistOperative());
        operative.tap();

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, operative))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CITIZEN);
        assertThat(gqs.hasCantBeBlocked(gd, operative)).isTrue();
    }

    @Test
    void untapAbilityRemovesTappedCharacteristics() {
        Permanent operative = addCreatureReady(player1, new FuturistOperative());
        operative.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(operative.isTapped()).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, operative))
                .doesNotContain(CardSubtype.CITIZEN);
        assertThat(gqs.hasCantBeBlocked(gd, operative)).isFalse();
    }

    @Test
    void countersStillModifyTappedBasePowerAndToughness() {
        Permanent operative = addCreatureReady(player1, new FuturistOperative());
        operative.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        operative.tap();

        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(3);
    }

    @Test
    void attackingOperativeCannotBeBlocked() {
        Permanent operative = addCreatureReady(player1, new FuturistOperative());
        harness.addToBattlefield(player2, new FuturistOperative());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(operative.isTapped()).isTrue();
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untappingRestoresOriginalCharacteristicsAndOnlyUntapsTheSource() {
        Permanent operative = harness.addToBattlefieldAndReturn(player1, new FuturistOperative());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FuturistOperative());
        int originalPower = gqs.getEffectivePower(gd, operative);
        int originalToughness = gqs.getEffectiveToughness(gd, operative);
        var originalSubtypes = List.copyOf(gqs.effectiveCreatureSubtypes(gd, operative));
        operative.tap();
        other.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(operative.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(operative.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, operative)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, operative)).isEqualTo(originalToughness);
        assertThat(gqs.effectiveCreatureSubtypes(gd, operative))
                .containsExactlyInAnyOrderElementsOf(originalSubtypes);
        assertThat(gqs.hasCantBeBlocked(gd, operative)).isFalse();
    }
}
