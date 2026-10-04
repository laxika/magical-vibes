package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GerminatingWurm.class})
class GerminatingWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and gains 2 life")
    void entersAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GerminatingWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Warp casts Germinating Wurm, gains 2 life, and exiles it at the next end step")
    void warpCastsAndExilesAtNextEndStep() {
        GerminatingWurm wurm = new GerminatingWurm();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Germinating Wurm");
        assertThat(gd.findExiledCard(wurm.getId())).isNull();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(wurm.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Germinating Wurm");
    }

    @Test
    void normalCastDoesNotExileAtEndStep() {
        GerminatingWurm wurm = new GerminatingWurm();
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Germinating Wurm");
        assertThat(gd.findExiledCard(wurm.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void warpedCardCanBeCastOnLaterTurnAndGainsLifeAgainWithoutAnotherExile() {
        GerminatingWurm wurm = new GerminatingWurm();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(wurm.getId())).isNotNull();

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, wurm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Germinating Wurm");
        assertThat(gd.findExiledCard(wurm.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersUnderOtherPlayersControlGainsLifeOnlyForThatPlayer() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.enterBattlefieldAndReturn(player2, new GerminatingWurm());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 17);
    }

    @Test
    void beingExiledWithoutWarpDoesNotGrantCastingPermission() {
        GerminatingWurm wurm = new GerminatingWurm();
        harness.setExile(player1, List.of(wurm));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromExile(player1, wurm.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(wurm.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Germinating Wurm");
        assertThat(gd.stack).isEmpty();
    }
}
