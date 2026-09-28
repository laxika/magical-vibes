package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({GiantFireBeetles.class, GrizzlyBears.class})
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
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }
}
