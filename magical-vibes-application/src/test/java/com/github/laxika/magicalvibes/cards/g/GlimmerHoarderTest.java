package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfSuffering;
import com.github.laxika.magicalvibes.cards.a.AphemiaTheCacophony;
import com.github.laxika.magicalvibes.cards.b.BalemurkLeech;
import com.github.laxika.magicalvibes.cards.d.DefilerOfFlesh;
import com.github.laxika.magicalvibes.cards.g.GravebreakerLamia;
import com.github.laxika.magicalvibes.cards.s.StarvingRevenant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimmerHoarder.class, AngelOfSuffering.class, AphemiaTheCacophony.class,
        BalemurkLeech.class, DefilerOfFlesh.class, GravebreakerLamia.class, StarvingRevenant.class})
class GlimmerHoarderTest extends BaseCardTest {

    @Test
    void tappedSurvivorLosesLifeAndDraftsAnEnchantmentCard() {
        Permanent hoarder = harness.addToBattlefieldAndReturn(player1, new GlimmerHoarder());
        hoarder.tap();

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SpellbookCardChoice.class);
        PendingInteraction.SpellbookCardChoice choice =
                (PendingInteraction.SpellbookCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).hasSize(3);

        Card selected = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, java.util.List.of(selected.getId()));

        Card drafted = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(selected.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(drafted.getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new GlimmerHoarder());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
