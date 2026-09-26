package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErayoSoratamiAscendant.class, ErayosEssence.class, Shock.class})
class ErayoSoratamiAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Flips when the fourth spell of the turn is cast by an opponent")
    void flipsOnFourthSpellCastByAnyPlayer() {
        Permanent erayo = harness.addToBattlefieldAndReturn(player1, new ErayoSoratamiAscendant());

        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, player1.getId());

        assertThat(erayo.isTransformed()).isFalse();

        harness.passBothPriorities();

        assertThat(erayo.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Essence counters an opponent's first spell after Erayo flips")
    void essenceCountersOpponentsFirstSpellAfterFlip() {
        Permanent erayo = harness.addToBattlefieldAndReturn(player1, new ErayoSoratamiAscendant());
        Shock fourthSpell = new Shock();
        Shock firstSpellAfterFlip = new Shock();

        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), fourthSpell));
        harness.setHand(player2, List.of(firstSpellAfterFlip));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(erayo.isTransformed()).isTrue();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(fourthSpell.getId()));

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(firstSpellAfterFlip.getId()));
    }

    @Test
    @DisplayName("Essence counters only an opponent's first spell each turn")
    void essenceCountersOnlyOpponentsFirstSpellEachTurn() {
        ErayoSoratamiAscendant front = new ErayoSoratamiAscendant();
        Permanent erayo = harness.addToBattlefieldAndReturn(player1, front);
        erayo.setTransformed(true);
        erayo.setCard(front.getBackFaceCard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        Shock firstOpponentSpell = new Shock();
        Shock secondOpponentSpell = new Shock();
        harness.setHand(player2, List.of(firstOpponentSpell, secondOpponentSpell));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        Shock firstOpponentSpellNextTurn = new Shock();
        harness.setHand(player2, List.of(firstOpponentSpellNextTurn));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The fourth-spell count resets at the start of a new turn")
    void fourthSpellCountResetsAtStartOfNewTurn() {
        Permanent erayo = harness.addToBattlefieldAndReturn(player1, new ErayoSoratamiAscendant());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(erayo.isTransformed()).isFalse();
    }
}
