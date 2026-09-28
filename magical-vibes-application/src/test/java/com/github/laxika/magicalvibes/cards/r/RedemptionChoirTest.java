package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({RedemptionChoir.class, GrizzlyBears.class, LlanowarElves.class, HillGiant.class})
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
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
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
