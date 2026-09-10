package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GomaFadaVanguard.class, FugitiveWizard.class})
class GomaFadaVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger stops an opponent's creature with power up to the Warrior count from blocking")
    void attackTriggerMakesCreatureUnableToBlock() {
        addCreatureReady(player1, new GomaFadaVanguard());
        addCreatureReady(player1, new GomaFadaVanguard());
        Permanent target = addCreatureReady(player2, new FugitiveWizard());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Opponent-controlled Warriors do not increase the target power limit")
    void opponentWarriorsDoNotCount() {
        Permanent vanguard = addCreatureReady(player1, new GomaFadaVanguard());
        Permanent opponentWarrior = addCreatureReady(player2, new GomaFadaVanguard());
        Permanent legalTarget = addCreatureReady(player2, new FugitiveWizard());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentWarrior.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();

        assertThat(opponentWarrior.isCantBlockThisTurn()).isFalse();
        assertThat(legalTarget.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature you control")
    void cannotTargetOwnCreature() {
        addCreatureReady(player1, new GomaFadaVanguard());
        Permanent ownCreature = addCreatureReady(player1, new FugitiveWizard());
        Permanent opponentCreature = addCreatureReady(player2, new FugitiveWizard());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
        assertThat(opponentCreature.isCantBlockThisTurn()).isTrue();
    }
}
