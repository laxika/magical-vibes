package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InquisitorEisenhorn.class, Divination.class, Forest.class})
class InquisitorEisenhornTest extends BaseCardTest {

    @Test
    @DisplayName("May reveal the first instant or sorcery drawn on any turn to create Cherubael")
    void revealsInstantOrSorceryAndCreatesCherubael() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new Divination(), new Forest()));
        gd.activePlayerId = player2.getId();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cherubael")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create Cherubael when the revealed card is not an instant or sorcery")
    void doesNotCreateCherubaelForOtherCardTypes() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Cherubael")).isEmpty();
    }

    @Test
    @DisplayName("Investigates once for each combat damage dealt to a player")
    void investigatesForCombatDamageAmount() {
        Permanent inquisitor = new Permanent(new InquisitorEisenhorn());
        inquisitor.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(inquisitor);
        inquisitor.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }
}
