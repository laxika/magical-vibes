package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResearchThief.class, Memnite.class, GrizzlyBears.class})
class ResearchThiefTest extends BaseCardTest {

    private Permanent addAttacker(Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }

    @Test
    @DisplayName("Each artifact creature dealing combat damage draws a card")
    void eachArtifactCreatureDamageDrawsACard() {
        harness.addToBattlefield(player1, new ResearchThief());
        addAttacker(new Memnite());
        addAttacker(new Memnite());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("A non-artifact creature dealing combat damage does not draw")
    void nonArtifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ResearchThief());
        addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Memnite()));

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Research Thief draws once for its own combat damage, regardless of damage amount")
    void drawsForItsOwnCombatDamage() {
        addAttacker(new ResearchThief());
        Card draw = new ResearchThief();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));

        resolveCombat();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 17);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("An opposing Research Thief does not trigger for your artifact creature")
    void opposingArtifactCreatureDoesNotTrigger() {
        harness.addToBattlefield(player2, new ResearchThief());
        addAttacker(new ResearchThief());
        Card draw = new ResearchThief();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(draw));
        harness.setLibrary(player2, List.of(new ResearchThief()));

        resolveCombat();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Research Thief triggers separately for the same artifact creature")
    void multipleCopiesTriggerSeparately() {
        addAttacker(new ResearchThief());
        harness.addToBattlefield(player1, new ResearchThief());
        Card firstDraw = new ResearchThief();
        Card secondDraw = new ResearchThief();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        resolveCombat();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }
}
