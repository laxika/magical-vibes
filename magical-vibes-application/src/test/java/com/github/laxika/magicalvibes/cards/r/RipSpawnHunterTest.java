package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RipSpawnHunter.class, GrizzlyBears.class, HillGiant.class, GiantSpider.class, Forest.class})
class RipSpawnHunterTest extends BaseCardTest {

    @Test
    void tappedSurvivorRevealsPowerCountAndOffersDistinctPowerCards() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        Card bears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card spider = new GiantSpider();
        Card nonCreature = new Forest();
        Card staysInLibrary = new Forest();
        rip.tap();
        harness.setLibrary(player1, List.of(bears, hillGiant, spider, nonCreature, staysInLibrary));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(bears, hillGiant, spider, nonCreature);
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), hillGiant.getId(), spider.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), hillGiant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears, hillGiant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(spider, nonCreature, staysInLibrary);
    }

    @Test
    void duplicatePowerSelectionIsRejected() {
        Permanent rip = harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        Card nonCreature = new Forest();
        rip.tap();
        harness.setLibrary(player1, List.of(firstBears, secondBears, hillGiant, nonCreature));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                firstBears, secondBears, hillGiant, nonCreature);
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new RipSpawnHunter());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
