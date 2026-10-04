package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakshasaDebaser.class, GrizzlyBears.class, Forest.class})
class RakshasaDebaserTest extends BaseCardTest {

    @Test
    void attackTriggerReturnsTargetCreatureFromDefendingPlayersGraveyard() {
        addReadyDebaser();
        Card returnedCreature = new GrizzlyBears();
        Card ownCreature = new GrizzlyBears();
        Card nonCreature = new Forest();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(returnedCreature, nonCreature));

        declareAttack();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(returnedCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(returnedCreature.getId()));
        resolveAttackTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(returnedCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(returnedCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(returnedCreature);
    }

    @Test
    void attackTriggerDoesNotTriggerWithoutCreatureInDefendingPlayersGraveyard() {
        addReadyDebaser();
        Card ownCreature = new GrizzlyBears();
        Card nonCreature = new Forest();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(nonCreature));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonCreature);
    }

    @Test
    void encoreCreatesHastyAttackingTokenCopyAndSacrificesItAtNextEndStep() {
        RakshasaDebaser debaser = new RakshasaDebaser();
        harness.setGraveyard(player1, List.of(debaser));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(debaser.getId()));

        advanceToEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent addReadyDebaser() {
        Permanent debaser = new Permanent(new RakshasaDebaser());
        debaser.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(debaser);
        return debaser;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    private void resolveAttackTrigger() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
