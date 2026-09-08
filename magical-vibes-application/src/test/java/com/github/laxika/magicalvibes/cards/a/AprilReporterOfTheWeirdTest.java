package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AprilReporterOfTheWeird.class, Forest.class, GrizzlyBears.class})
class AprilReporterOfTheWeirdTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws that many cards, then discards one")
    void combatDamageDrawsThatManyThenDiscardsOne() {
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        Card cardToKeep = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(cardToKeep)));
        harness.setLibrary(player1, new ArrayList<>(List.of(firstDraw, secondDraw, new Forest())));
        addAttackingApril();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when no combat damage reaches a player")
    void doesNotTriggerWhenBlocked() {
        Permanent april = addAttackingApril();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(april);
    }

    private Permanent addAttackingApril() {
        Permanent april = addCreatureReady(player1, new AprilReporterOfTheWeird());
        april.setAttacking(true);
        return april;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
