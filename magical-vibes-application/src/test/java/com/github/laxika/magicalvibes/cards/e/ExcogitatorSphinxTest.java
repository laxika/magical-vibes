package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExcogitatorSphinx.class, Clue.class, Divination.class, GrizzlyBears.class})
class ExcogitatorSphinxTest extends BaseCardTest {

    @Test
    void investigatesOnceWhenMultipleCreaturesDealCombatDamage() {
        addReadyPermanent(player1, new ExcogitatorSphinx());
        addReadyPermanent(player1, new GrizzlyBears());
        addReadyPermanent(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificesAClueToSeekAnInstantOrSorcery() {
        Permanent sphinx = addReadyPermanent(player1, new ExcogitatorSphinx());
        harness.addToBattlefield(player1, new Clue());
        harness.addToBattlefield(player1, new Clue());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sphinx),
                0, null, null);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, choice.validIds().iterator().next());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(Divination.class::isInstance);
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player,
                                        com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
