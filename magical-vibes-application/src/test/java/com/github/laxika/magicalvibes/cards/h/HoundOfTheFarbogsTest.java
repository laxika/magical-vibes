package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PortTown;
import com.github.laxika.magicalvibes.cards.g.GhoulcallersAccomplice;
import com.github.laxika.magicalvibes.cards.e.ExplosiveApparatus;
import com.github.laxika.magicalvibes.cards.j.JacesScrutiny;
import com.github.laxika.magicalvibes.cards.w.WickerWitch;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoundOfTheFarbogs.class, GhoulcallersAccomplice.class, PortTown.class,
        JacesScrutiny.class, ExplosiveApparatus.class, WickerWitch.class})
class HoundOfTheFarbogsTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have menace without delirium")
    void noDeliriumNoMenace() {
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        assertThat(gqs.hasKeyword(gd, findHound(), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has menace with four card types in its controller's graveyard")
    void deliriumGrantsMenace() {
        setDelirium();
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        assertThat(gqs.hasKeyword(gd, findHound(), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GhoulcallersAccomplice(), new PortTown(), new JacesScrutiny(), new ExplosiveApparatus()));
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        assertThat(gqs.hasKeyword(gd, findHound(), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Loses menace when its controller's graveyard drops below four card types")
    void losesMenaceWhenGraveyardChanges() {
        setDelirium();
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        Permanent hound = findHound();
        assertThat(gqs.hasKeyword(gd, hound, Keyword.MENACE)).isTrue();

        harness.setGraveyard(player1, List.of(new GhoulcallersAccomplice(), new PortTown(), new JacesScrutiny()));

        assertThat(gqs.hasKeyword(gd, hound, Keyword.MENACE)).isFalse();
    }

    @Test
    void fourCardsWithOnlyThreeTypesDoNotGrantMenace() {
        harness.setGraveyard(player1, List.of(new GhoulcallersAccomplice(),
                new GhoulcallersAccomplice(), new PortTown(), new JacesScrutiny()));
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        assertThat(gqs.hasKeyword(gd, findHound(), Keyword.MENACE)).isFalse();
    }

    @Test
    void artifactCreatureCountsAsTwoTypes() {
        harness.setGraveyard(player1, List.of(new WickerWitch(), new PortTown(), new JacesScrutiny()));
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        assertThat(gqs.hasKeyword(gd, findHound(), Keyword.MENACE)).isTrue();
    }

    @Test
    void gainsMenaceWhenGraveyardReachesFourTypes() {
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());
        Permanent hound = findHound();
        assertThat(gqs.hasKeyword(gd, hound, Keyword.MENACE)).isFalse();

        setDelirium();

        assertThat(gqs.hasKeyword(gd, hound, Keyword.MENACE)).isTrue();
    }

    @Test
    void deliriumPreventsSingleBlocker() {
        setDelirium();
        addCreatureReady(player1, new HoundOfTheFarbogs());
        addCreatureReady(player2, new GhoulcallersAccomplice());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void deliriumAllowsTwoBlockers() {
        setDelirium();
        addCreatureReady(player1, new HoundOfTheFarbogs());
        Permanent first = addCreatureReady(player2, new GhoulcallersAccomplice());
        Permanent second = addCreatureReady(player2, new GhoulcallersAccomplice());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    void withoutDeliriumAllowsSingleBlocker() {
        addCreatureReady(player1, new HoundOfTheFarbogs());
        Permanent blocker = addCreatureReady(player2, new GhoulcallersAccomplice());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GhoulcallersAccomplice(), new PortTown(), new JacesScrutiny(), new ExplosiveApparatus()));
    }

    private Permanent findHound() {
        return findPermanent(player1, "Hound of the Farbogs");
    }
}
