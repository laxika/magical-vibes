package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CovertCutpurse.class, CovetousGeist.class, GrizzlyBears.class})
class CovertCutpurseTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys an opponent's creature dealt damage this turn")
    void etbDestroysDamagedOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Disturb enters transformed as Covetous Geist")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent geist = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(geist.isTransformed()).isTrue();
        assertThat(geist.getCard()).isInstanceOf(CovetousGeist.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Covetous Geist is exiled instead of going to the graveyard")
    void covetousGeistIsExiledInsteadOfGraveyard() {
        Permanent geist = putTransformedGeistOnBattlefield();
        UUID cardId = geist.getOriginalCard().getId();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geist));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    private Permanent putTransformedGeistOnBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CovertCutpurse()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
