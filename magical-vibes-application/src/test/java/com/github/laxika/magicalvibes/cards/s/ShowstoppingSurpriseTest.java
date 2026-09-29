package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShowstoppingSurprise.class, GrizzlyBears.class, HillGiant.class})
class ShowstoppingSurpriseTest extends BaseCardTest {

    @Test
    void damagesEachOtherCreatureButNotTheTargetOrPlayers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(target);
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void turnsFaceDownTargetFaceUpBeforeUsingItsPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(target);
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetCreatureAnOpponentControls() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShowstoppingSurprise()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new ShowstoppingSurprise()));
        harness.addMana(player1, ManaColor.RED, 5);
        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);
    }
}
