package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherealUsher.class, FountainOfYouth.class, GrizzlyBears.class, CrawWurm.class})
class EtherealUsherTest extends BaseCardTest {

    @Test
    void activatedAbilityMakesTargetCreatureUnblockableThisTurn() {
        Permanent usher = addReadyCreature(player1, new EtherealUsher());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, usher), 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        beginBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent usher = addReadyCreature(player1, new EtherealUsher());
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, indexOf(player1, usher), 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent usher = addReadyCreature(player1, new EtherealUsher());
        Permanent fountain = addReadyPermanent(player2, new FountainOfYouth());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, usher), 0, null, fountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        EtherealUsher usher = new EtherealUsher();
        CrawWurm matchingCard = new CrawWurm();
        GrizzlyBears differentManaValue = new GrizzlyBears();
        harness.setHand(player1, List.of(usher));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Ethereal Usher");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addReadyPermanent(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
