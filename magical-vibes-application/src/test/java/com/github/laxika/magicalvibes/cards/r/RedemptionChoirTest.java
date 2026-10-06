package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({RedemptionChoir.class, GrizzlyBears.class, LlanowarElves.class, HillGiant.class, Pacifism.class, Shock.class, Forest.class})
class RedemptionChoirTest extends BaseCardTest {

    @Test
    void entersAndReturnsPermanentWhenCovenIsMet() {
        addCovenCreatures();
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new RedemptionChoir()));
        addChoirMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        chooseReturnedCard(returned);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNotReturnPermanentWhenCovenIsNotMet() {
        addReadyCreature(player1, new RedemptionChoir());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void attacksAndReturnsPermanentWhenCovenIsMet() {
        addReadyCreature(player1, new RedemptionChoir());
        addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new HillGiant());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        declareAttack();
        chooseReturnedCard(returned);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void enteringChoirCountsTowardCoven() {
        addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new GrizzlyBears());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new RedemptionChoir()));
        addChoirMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chooseReturnedCard(returned);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void threeCreaturesWithOnlyTwoDifferentPowersDoNotMeetCoven() {
        addReadyCreature(player1, new RedemptionChoir());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));

        declareAttack();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void opponentsCreaturesDoNotCountTowardCoven() {
        addReadyCreature(player1, new RedemptionChoir());
        addReadyCreature(player2, new LlanowarElves());
        addReadyCreature(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttack();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void targetChoiceExcludesExpensivePermanentsNonpermanentsAndOpponentsCards() {
        addReadyCreature(player1, new RedemptionChoir());
        addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new GrizzlyBears());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned, new HillGiant(), new Shock()));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));

        declareAttack();
        chooseReturnedCard(returned);

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void doesNotReturnTargetIfCovenIsLostBeforeResolution() {
        addReadyCreature(player1, new RedemptionChoir());
        Permanent elf = addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new GrizzlyBears());
        Card returned = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned));

        declareAttack();
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, elf.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
    }

    @Test
    void returnedAuraEntersAttachedToChosenCreature() {
        addReadyCreature(player1, new RedemptionChoir());
        addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new GrizzlyBears());
        Permanent host = addReadyCreature(player2, new HillGiant());
        Card returned = new Pacifism();
        harness.setGraveyard(player1, List.of(returned));

        declareAttack();
        chooseReturnedCard(returned);

        assertThat(gd.interaction.pendingAuraCard()).isNotNull();
        harness.handlePermanentChosen(player1, host.getId());

        harness.assertOnBattlefield(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(returned.getId());
                    assertThat(permanent.getAttachedTo()).isEqualTo(host.getId());
                });
    }
    @Test
    void returnsLandWithManaValueZero() {
        addReadyCreature(player1, new RedemptionChoir());
        addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new GrizzlyBears());
        Card returned = new Forest();
        harness.setGraveyard(player1, List.of(returned));

        declareAttack();
        chooseReturnedCard(returned);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void enteringWithoutCovenDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RedemptionChoir()));
        addChoirMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
    private void addCovenCreatures() {
        addReadyCreature(player1, new LlanowarElves());
        addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new HillGiant());
    }

    private void addChoirMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    private void chooseReturnedCard(Card returned) {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(returned.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();
    }
}
