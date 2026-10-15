package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HarvestHand;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScourgeWolf.class, Plains.class, LeoninScimitar.class, Pacifism.class, Shock.class,
        HarvestHand.class})
class ScourgeWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike with fewer than four card types in its controller's graveyard")
    void noDoubleStrikeBelowDelirium() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(new Plains(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike with four card types in its controller's graveyard")
    void hasDoubleStrikeWithDelirium() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not count card types in an opponent's graveyard")
    void opponentGraveyardDoesNotCount() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(new Plains(), new Shock(), new LeoninScimitar()));
        harness.setGraveyard(player2, List.of(new Pacifism()));

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike updates immediately when delirium is gained and lost")
    void doubleStrikeTracksGraveyardChanges() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(new Plains(), new LeoninScimitar(), new Pacifism()));
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.setGraveyard(player1, List.of(
                new Plains(), new LeoninScimitar(), new Pacifism(), new Shock()));
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.setGraveyard(player1, List.of(new Plains(), new LeoninScimitar(), new Pacifism()));
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Multiple cards of the same type do not satisfy delirium")
    void duplicateTypesDoNotCountSeparately() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(
                new Plains(), new Plains(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An artifact creature contributes both types and double strike applies only to the wolf")
    void multipleTypesOnOneCardCountAndGrantIsSelfOnly() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new ScourgeWolf());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HarvestHand());
        harness.setGraveyard(player1, List.of(new Plains(), new HarvestHand(), new Pacifism()));

        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Without delirium an unblocked wolf deals damage only once")
    void unblockedWithoutDeliriumDealsTwoDamage() {
        addCreatureReady(player1, new ScourgeWolf());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("With delirium an unblocked wolf deals damage in both combat damage steps")
    void unblockedWithDeliriumDealsFourDamage() {
        addCreatureReady(player1, new ScourgeWolf());
        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }
}
