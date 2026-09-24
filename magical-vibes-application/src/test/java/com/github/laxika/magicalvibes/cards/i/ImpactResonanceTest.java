package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpactResonance.class, Pyroclasm.class, GiantSpider.class})
class ImpactResonanceTest extends BaseCardTest {

    @Test
    void usesTheGreatestDamageToOneRecipientNotTheSourceTotal() {
        harness.forceActivePlayer(player1);
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player1, List.of(new ImpactResonance()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, Map.of(firstTarget.getId(), 1, secondTarget.getId(), 1));
        harness.passBothPriorities();

        assertThat(firstTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void requiresAssignmentsToSumToTheCastTimeDamageValue() {
        harness.forceActivePlayer(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player1, List.of(new ImpactResonance()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(target.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
