package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinotaJoinerOfForces.class, EliteVanguard.class, GrizzlyBears.class})
class WinotaJoinerOfForcesTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Human attacker puts a Human onto the battlefield tapped and attacking")
    void nonHumanAttackerPutsHumanOntoBattlefieldAttacking() {
        addCreatureReady(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new GrizzlyBears());
        EliteVanguard human = new EliteVanguard();
        harness.setLibrary(player1, List.of(
                human,
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice libraryChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(libraryChoice).isNotNull();
        assertThat(libraryChoice.validCardIds()).containsExactly(human.getId());

        harness.handleMultipleCardsChosen(player1, List.of(human.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent enteredHuman = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == human)
                .findFirst()
                .orElseThrow();
        assertThat(enteredHuman.isTapped()).isTrue();
        assertThat(enteredHuman.isAttacking()).isTrue();
        assertThat(enteredHuman.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, enteredHuman, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A Human attacker does not trigger Winota")
    void humanAttackerDoesNotTrigger() {
        harness.addToBattlefield(player1, new WinotaJoinerOfForces());
        addCreatureReady(player1, new EliteVanguard());
        harness.setLibrary(player1, List.of(new EliteVanguard()));

        declareAttackers(List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
