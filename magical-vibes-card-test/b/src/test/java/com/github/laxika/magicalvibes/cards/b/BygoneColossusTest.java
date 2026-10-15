package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BygoneColossus.class)
class BygoneColossusTest extends BaseCardTest {

    @Test
    void warpCastsAndExilesAtTheNextEndStep() {
        BygoneColossus colossus = new BygoneColossus();
        harness.setHand(player1, List.of(colossus));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(colossus.getId()));

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(colossus.getId())).isNotNull();
    }

    @Test
    void warpExileWaitsForItsDelayedTriggerToResolve() {
        harness.setHand(player1, List.of(new BygoneColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Bygone Colossus");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bygone Colossus");
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card() instanceof BygoneColossus);
    }

    @Test
    void payingNormalManaCostDoesNotScheduleWarpExile() {
        harness.castFromHand(player1, new BygoneColossus(), "{9}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Bygone Colossus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void genericWarpCostCanBePaidWithColoredMana() {
        harness.setHand(player1, List.of(new BygoneColossus()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bygone Colossus");
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Bygone Colossus");
    }

    @Test
    void warpedCardCanBeCastOnALaterTurnForItsNormalCostAndStaysOnBattlefield() {
        BygoneColossus colossus = new BygoneColossus();
        harness.setHand(player1, List.of(colossus));
        harness.setLibrary(player1, List.of(new BygoneColossus(), new BygoneColossus()));
        harness.setLibrary(player2, List.of(new BygoneColossus(), new BygoneColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.findExiledCard(colossus.getId())).isNotNull();
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        assertThatThrownBy(() -> harness.castFromExile(player1, colossus.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, colossus.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(colossus.getId())).isNotNull();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castFromExile(player1, colossus.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bygone Colossus");
        assertThat(gd.findExiledCard(colossus.getId())).isNull();
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Bygone Colossus");
        assertThat(gd.stack).isEmpty();
    }
}
