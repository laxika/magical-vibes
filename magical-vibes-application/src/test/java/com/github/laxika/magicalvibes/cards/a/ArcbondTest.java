package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArcbondTest extends BaseCardTest {

    @Test
    @DisplayName("The watched creature deals that much damage to each other creature and each player")
    void dealsDamageToOtherCreaturesAndPlayers() {
        Permanent watched = addCreatureReady(player2, new AirElemental());
        Permanent friendly = addCreatureReady(player1, new AirElemental());
        Permanent opposing = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new Arcbond(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, watched.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, watched.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(watched.getMarkedDamage()).isEqualTo(2);
        assertThat(friendly.getMarkedDamage()).isEqualTo(2);
        assertThat(opposing.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The delayed trigger expires at end of turn")
    void triggerExpiresAtEndOfTurn() {
        Permanent watched = addCreatureReady(player2, new AirElemental());
        Permanent other = addCreatureReady(player1, new AirElemental());

        harness.setHand(player1, List.of(new Arcbond()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, watched.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, watched.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(watched.getMarkedDamage()).isEqualTo(2);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        UUID forestId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();

        harness.setHand(player1, List.of(new Arcbond()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }
}
