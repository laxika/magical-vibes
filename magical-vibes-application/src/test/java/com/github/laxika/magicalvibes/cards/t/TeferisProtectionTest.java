package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GruesomeFate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisProtection.class, GrizzlyBears.class, GruesomeFate.class})
class TeferisProtectionTest extends BaseCardTest {

    @Test
    void phasesOutPermanentsExilesItAndPreventsLifeTotalChanges() {
        harness.setLife(player1, 10);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card protection = new TeferisProtection();
        harness.setHand(player1, List.of(protection));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(ownCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(protection);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from everything");

        harness.setHand(player2, List.of(new GruesomeFate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    void phasesInAndAllowsLifeTotalChangesAtNextTurn() {
        harness.setLife(player1, 10);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TeferisProtection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GruesomeFate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 9);
    }
}
