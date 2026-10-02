package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShoveAside.class, GrizzlyBears.class, Mountain.class})
class ShoveAsideTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShoveAside()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new ShoveAside()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonStartingPlayerMayCastItOutsideTheirMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.startingPlayerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.setHand(player2, List.of(new ShoveAside()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorcery(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void startingPlayerCannotCastItOutsideTheirMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.startingPlayerId = player1.getId();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ShoveAside()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
