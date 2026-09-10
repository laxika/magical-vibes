package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StingBilbosSword.class, GrizzlyBears.class})
class StingBilbosSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Sting counts creatures controlled by the targeted opponent and attaches to your creature")
    void countsTargetOpponentsCreaturesAndAttaches() {
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castSting(player2.getId(), host.getId());

        Permanent sting = findPermanent(player1, "Sting, Bilbo's Sword");
        assertThat(sting.getCounterCount(CounterType.HONE)).isEqualTo(2);
        assertThat(sting.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sting may enter without attaching")
    void mayEnterWithoutAttaching() {
        addCreatureReady(player2, new GrizzlyBears());

        castSting(player2.getId(), null);

        Permanent sting = findPermanent(player1, "Sting, Bilbo's Sword");
        assertThat(sting.getCounterCount(CounterType.HONE)).isEqualTo(1);
        assertThat(sting.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The ETB attachment target must be a creature you control")
    void attachmentCannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new StingBilbosSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), opponentCreature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void castSting(UUID opponentId, UUID creatureId) {
        harness.setHand(player1, List.of(new StingBilbosSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<UUID> targetIds = creatureId == null
                ? List.of(opponentId)
                : List.of(opponentId, creatureId);
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
