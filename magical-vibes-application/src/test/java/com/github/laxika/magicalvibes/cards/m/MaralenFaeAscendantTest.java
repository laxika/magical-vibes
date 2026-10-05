package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaralenFaeAscendant.class, ElvishWarrior.class, GrizzlyBears.class, Opt.class,
        Bitterblossom.class, MindSpring.class})
class MaralenFaeAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Another Elf entering exiles two cards from a target opponent's library")
    void qualifyingElfEntryExilesTwoCards() {
        Permanent maralen = harness.addToBattlefieldAndReturn(player1, new MaralenFaeAscendant());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(maralen.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Maralen permits one current-turn nonland spell for free up to the Elf and Faerie count")
    void permitsOneCurrentTurnSpellWithinSubtypeCount() {
        Permanent maralen = addReadyMaralen();
        Opt first = new Opt();
        Opt second = new Opt();
        GrizzlyBears tooExpensive = new GrizzlyBears();

        gd.addToExile(player2.getId(), first, maralen.getId());
        gd.addToExile(player2.getId(), second, maralen.getId());
        gd.addToExile(player2.getId(), tooExpensive, maralen.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, tooExpensive.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, first.getId());

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void selfEntryExilesEvenWithOnlyOneCardInLibrary() {
        harness.setLibrary(player2, List.of(new Opt()));
        Permanent maralen = harness.enterBattlefieldAndReturn(player1, new MaralenFaeAscendant());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(maralen.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void noncreatureFaerieEntryExilesTwoCards() {
        Permanent maralen = addReadyMaralen();
        harness.setLibrary(player2, List.of(new Opt(), new Opt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Bitterblossom()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(maralen.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void unrelatedCreatureAndOpponentsElfDoNotTrigger() {
        Permanent maralen = addReadyMaralen();
        harness.setLibrary(player2, List.of(new Opt(), new Opt()));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new ElvishWarrior());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(maralen.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    void noncreatureFaerieCountsTowardManaValueLimit() {
        Permanent maralen = addReadyMaralen();
        harness.addToBattlefield(player1, new Bitterblossom());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player2.getId(), spell, maralen.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromExile(player1, spell.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
    }

    @Test
    void cardsExiledOnPreviousTurnCannotBeCast() {
        Permanent maralen = addReadyMaralen();
        Opt spell = new Opt();
        gd.addToExile(player2.getId(), spell, maralen.getId());
        gd.turnNumber++;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void permissionEndsWhenMaralenLeavesBattlefield() {
        Permanent maralen = addReadyMaralen();
        Opt spell = new Opt();
        gd.addToExile(player2.getId(), spell, maralen.getId());
        gd.playerBattlefields.get(player1.getId()).remove(maralen);
        gd.playerGraveyards.get(player1.getId()).add(maralen.getCard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void instantCanBeCastOnOpponentsTurn() {
        Permanent maralen = addReadyMaralen();
        Opt spell = new Opt();
        gd.addToExile(player2.getId(), spell, maralen.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.castFromExile(player1, spell.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void creatureCannotBeCastOnOpponentsTurn() {
        Permanent maralen = addReadyMaralen();
        harness.addToBattlefield(player1, new ElvishWarrior());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player2.getId(), spell, maralen.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void freeSpellWithXMustUseZero() {
        Permanent maralen = addReadyMaralen();
        harness.addToBattlefield(player1, new ElvishWarrior());
        MindSpring spell = new MindSpring();
        gd.addToExile(player2.getId(), spell, maralen.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, spell.getId(), 3, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyMaralen() {
        return addCreatureReady(player1, new MaralenFaeAscendant());
    }
}
