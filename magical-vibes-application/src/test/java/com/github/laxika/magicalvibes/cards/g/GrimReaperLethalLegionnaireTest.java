package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimReaperLethalLegionnaire.class, GrizzlyBears.class, Shock.class})
class GrimReaperLethalLegionnaireTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers a creature card from the graveyard and returns it tapped and attacking with a finality counter")
    void returnsTargetCreatureTappedAndAttackingWithFinalityCounter() {
        GrimReaperLethalLegionnaire grimReaper = new GrimReaperLethalLegionnaire();
        addReadyPermanent(grimReaper);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttack();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanentByCardId(creature.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the creature in the graveyard")
    void decliningPaymentLeavesCreatureInGraveyard() {
        GrimReaperLethalLegionnaire grimReaper = new GrimReaperLethalLegionnaire();
        addReadyPermanent(grimReaper);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        declareAttack();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The attack trigger only targets creature cards")
    void onlyTargetsCreatureCards() {
        GrimReaperLethalLegionnaire grimReaper = new GrimReaperLethalLegionnaire();
        addReadyPermanent(grimReaper);
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(noncreature));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
    }

    private Permanent addReadyPermanent(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
