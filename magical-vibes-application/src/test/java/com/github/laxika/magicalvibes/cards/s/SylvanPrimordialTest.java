package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SylvanPrimordialTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the targeted noncreature permanent and tutors a tapped Forest")
    void destroysTargetAndFetchesForest() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        castSylvanPrimordial(List.of(plains.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(plains.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent fetched = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        assertThat(fetched.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No target chosen means nothing is destroyed and no search happens")
    void noTargetsMeansNoSearch() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setLibrary(player1, List.of(new Forest()));

        castSylvanPrimordial(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(plains.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent its controller controls")
    void cannotTargetOwnPermanent() {
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownPlains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSylvanPrimordial(List<UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new SylvanPrimordial()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
