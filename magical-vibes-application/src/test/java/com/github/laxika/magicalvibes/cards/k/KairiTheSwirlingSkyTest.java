package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CoilingStalker;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvokeDespair;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuitUp;
import com.github.laxika.magicalvibes.cards.t.TamiyosSafekeeping;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KairiTheSwirlingSky.class, GrizzlyBears.class, HillGiant.class, MindStone.class,
        Shock.class, YavimayaWurm.class, CoilingStalker.class, InvokeDespair.class,
        TamiyosSafekeeping.class, SuitUp.class})
class KairiTheSwirlingSkyTest extends BaseCardTest {

    private static final String RETURN_MODE =
            "Return any number of target nonland permanents with total mana value 6 or less to their owners' hands";
    private static final String MILL_MODE =
            "Mill six cards, then return up to two instant and/or sorcery cards from your graveyard to your hand";

    @Test
    void deathTriggerReturnsTargetedPermanentsWithinTotalManaValue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent highManaValue = harness.addToBattlefieldAndReturn(player2, new YavimayaWurm());
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, RETURN_MODE);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(bears.getId(), giant.getId(), stone.getId(), highManaValue.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(giant.getId(), stone.getId())
                .doesNotContain(highManaValue.getId());

        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(permanent -> permanent.getId())
                .doesNotContain(bears.getId(), giant.getId()).contains(highManaValue.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getId())
                .contains(stone.getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(bears.getCard().getId(), giant.getCard().getId());
    }

    @Test
    void deathTriggerMillsThenReturnsUpToTwoInstantOrSorceryCards() {
        Card firstShock = new Shock();
        Card secondShock = new Shock();
        harness.setLibrary(player1, List.of(
                firstShock, secondShock,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, MILL_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(firstShock));
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(secondShock));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(firstShock.getId(), secondShock.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(4);
    }

    @Test
    void returnModeMayChooseNoTargetsEvenWhenTargetsAreAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CoilingStalker());
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returnModeCanReturnBothControllersPermanentsAndStopBelowTheLimit() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CoilingStalker());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CoilingStalker());
        Permanent unchosenCreature = harness.addToBattlefieldAndReturn(player2, new CoilingStalker());
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, RETURN_MODE);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownCreature.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opposingCreature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unchosenCreature)
                .doesNotContain(opposingCreature);
    }

    @Test
    void millModeMayDeclineAllReturnsAndMillsExactlySixCards() {
        Card instant = new TamiyosSafekeeping();
        Card sorcery = new InvokeDespair();
        Card seventhCard = new CoilingStalker();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setLibrary(player1, List.of(instant, new CoilingStalker(), new CoilingStalker(),
                new CoilingStalker(), new CoilingStalker(), new CoilingStalker(), seventhCard));
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, MILL_MODE);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(seventhCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant, sorcery)
                .doesNotContain(seventhCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millModeMayReturnOnePreexistingSorceryAndLeaveOtherSpells() {
        Card sorcery = new InvokeDespair();
        Card instant = new TamiyosSafekeeping();
        Card opposingSpell = new InvokeDespair();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setGraveyard(player2, List.of(opposingSpell));
        harness.setLibrary(player1, List.of(instant, new CoilingStalker()));
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, MILL_MODE);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(sorcery));
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant).doesNotContain(sorcery);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingSpell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millModeFinishesWithAShortLibraryAndNoEligibleCards() {
        Card creature = new CoilingStalker();
        harness.setLibrary(player1, List.of(creature));
        harness.addToBattlefield(player1, new KairiTheSwirlingSky());

        killKairi();
        harness.handleListChoice(player1, MILL_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPayThreeMana() {
        Permanent kairi = harness.addToBattlefieldAndReturn(player1, new KairiTheSwirlingSky());
        Card spell = new SuitUp();
        Card topCard = new CoilingStalker();
        harness.setHand(player2, List.of(spell));
        harness.setLibrary(player2, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, kairi.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kairi);
        assertThat(gd.stack).isEmpty();
    }

    private void killKairi() {
        Permanent kairi = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KairiTheSwirlingSky)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kairi));
        harness.passBothPriorities();
    }
}
