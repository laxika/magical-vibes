package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantFireBeetles.class})
class GiantFireBeetlesTest extends BaseCardTest {

    @Test
    @DisplayName("Double team conjures a copy and removes itself from both cards")
    void doubleTeamConjuresCopyAndRemovesItselfFromBothCards() {
        Permanent beetles = addCreatureReady(player1, new GiantFireBeetles());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, beetles, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Giant Fire Beetles"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }

    @Test
    @DisplayName("Menace prevents Giant Fire Beetles from being blocked by one creature")
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new GiantFireBeetles());
        addCreatureReady(player2, new GiantFireBeetles());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two creatures to block Giant Fire Beetles")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new GiantFireBeetles());
        addCreatureReady(player2, new GiantFireBeetles());
        addCreatureReady(player2, new GiantFireBeetles());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    @Test
    @DisplayName("The original and duplicate do not double team on later attacks")
    void originalAndDuplicateDoNotConjureAgain() {
        Permanent original = addCreatureReady(player1, new GiantFireBeetles());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).removeFirst();
        addCreatureReady(player1, duplicate);
        original.untap();

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Double team remains lost when the original leaves and returns")
    void originalDoesNotRegainDoubleTeamAfterReturning() {
        Permanent original = addCreatureReady(player1, new GiantFireBeetles());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());
        Card returned = gd.playerGraveyards.get(player1.getId()).removeFirst();
        addCreatureReady(player1, returned);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
