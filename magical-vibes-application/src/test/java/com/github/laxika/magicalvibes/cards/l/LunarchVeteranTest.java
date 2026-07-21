package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LunarchVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new LunarchVeteran());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve bears
        harness.passBothPriorities(); // resolve life trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when it enters itself")
    void noLifeOnOwnEnter() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LunarchVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Disturb casts from graveyard transformed as Luminous Phantom")
    void disturbEntersTransformed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LunarchVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent phantom = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(phantom.isTransformed()).isTrue();
        assertThat(phantom.getCard().getName()).isEqualTo("Luminous Phantom");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Luminous Phantom gains life when another creature you control leaves")
    void phantomGainsLifeOnAllyCreatureLeaves() {
        harness.setLife(player1, 20);
        Permanent phantom = putTransformedPhantomOnBattlefield();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears);
        harness.passBothPriorities(); // resolve leave trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(phantom);
    }

    @Test
    @DisplayName("Luminous Phantom is exiled instead of going to the graveyard")
    void phantomExiledInsteadOfGraveyard() {
        Permanent phantom = putTransformedPhantomOnBattlefield();
        UUID phantomId = phantom.getOriginalCard().getId();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, phantom);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getId())).contains(phantomId);
    }

    private Permanent putTransformedPhantomOnBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LunarchVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
