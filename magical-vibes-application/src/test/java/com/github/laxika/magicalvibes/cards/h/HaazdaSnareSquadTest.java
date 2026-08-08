package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HaazdaSnareSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {W} taps the targeted opponent creature")
    void payingTapsTargetCreature() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the payment leaves the target untapped")
    void decliningLeavesTargetUntapped() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No target selection when the opponent controls no creatures")
    void noTargetSelectionWithoutOpponentCreature() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        harness.addMana(player1, ManaColor.WHITE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Own creatures are not legal targets for the attack trigger")
    void ownCreatureNotTapped() {
        addCreatureReady(player1, new HaazdaSnareSquad());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).getFirst();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(opponentBears.isTapped()).isTrue();
        assertThat(ownBears.isTapped()).isFalse();
    }
}
