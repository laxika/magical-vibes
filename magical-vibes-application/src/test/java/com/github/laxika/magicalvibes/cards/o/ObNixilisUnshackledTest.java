package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ObNixilisUnshackledTest extends BaseCardTest {

    // ===== Trigger: opponent searches their library =====

    @Test
    @DisplayName("Opponent searching their library sacrifices a creature and loses 10 life")
    void opponentSearchSacrificesAndDrainsTen() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        castTutorAndFinishSearch();

        harness.passBothPriorities(); // resolve Ob Nixilis' search trigger

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);

        // The sacrificed creature dying also triggers Ob Nixilis' own +1/+1 counter ability.
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Ob Nixilis, Unshackled")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The searching player chooses which creature to sacrifice")
    void searchingPlayerChoosesSacrifice() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerBattlefields.get(player1.getId()).add(spider);

        castTutorAndFinishSearch();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Life loss still happens when the searching player controls no creature")
    void losesLifeWithoutCreature() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLife(player1, 20);

        castTutorAndFinishSearch();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's own library search does not trigger it")
    void ownSearchDoesNotTrigger() {
        harness.addToBattlefield(player1, new ObNixilisUnshackled());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        castTutorAndFinishSearch();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    // ===== Trigger: another creature dies =====

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature dies")
    void getsCounterWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new ObNixilisUnshackled());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent obNixilis = findPermanent(player1, "Ob Nixilis, Unshackled");
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Shock resolves, Grizzly Bears dies
        harness.passBothPriorities(); // Ob Nixilis' counter trigger resolves

        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, obNixilis)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, obNixilis)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not get a counter when Ob Nixilis itself dies")
    void noCounterWhenItselfDies() {
        harness.addToBattlefield(player1, new ObNixilisUnshackled());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        UUID obId = harness.getPermanentId(player1, "Ob Nixilis, Unshackled");
        harness.castInstant(player2, 0, obId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, obId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ob Nixilis, Unshackled");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    /** Player 1 casts Diabolic Tutor and completes the library search it starts. */
    private void castTutorAndFinishSearch() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp()));

        harness.setHand(player1, List.of(new DiabolicTutor()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }
}
