package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoordinatedClobbering.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class})
class CoordinatedClobberingTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two chosen creatures and has each deal its power to the target")
    void tapsTwoCreaturesAndDealsTheirPower() {
        Permanent firstSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondSource = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(List.of(victim.getId(), firstSource.getId(), secondSource.getId()));
        harness.passBothPriorities();

        assertThat(firstSource.isTapped()).isTrue();
        assertThat(secondSource.isTapped()).isTrue();
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows choosing only one source creature")
    void allowsOneSourceCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(List.of(victim.getId(), source.getId()));
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires untapped creatures controlled by the caster as sources")
    void rejectsTappedSource() {
        Permanent tappedSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        tappedSource.tap();
        harness.setHand(player1, List.of(new CoordinatedClobbering()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(
                victim.getId(), tappedSource.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creatures you control");
    }

    @Test
    @DisplayName("Requires a creature an opponent controls as the victim")
    void rejectsOwnVictim() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownVictim = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new CoordinatedClobbering()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(
                ownVictim.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new CoordinatedClobbering()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, targets);
    }
}
