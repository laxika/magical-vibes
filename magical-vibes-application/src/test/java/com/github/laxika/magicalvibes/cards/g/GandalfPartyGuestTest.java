package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzamiLadyOfScrolls;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfPartyGuest.class, AzamiLadyOfScrolls.class, CounselOfTheSoratami.class,
        GrizzlyBears.class})
class GandalfPartyGuestTest extends BaseCardTest {

    @Test
    @DisplayName("A spell above twice the legendary Wizard count is not offered")
    void doesNotOfferSpellAboveTwiceTheLegendaryWizardCount() {
        addGandalf();
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(spell));

        advanceToCombat(player1);

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Additional legendary Wizards increase the free-cast mana-value limit")
    void additionalLegendaryWizardIncreasesLimit() {
        addGandalf();
        addCreatureReady(player1, new AzamiLadyOfScrolls());
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(spell));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger offers only instant and sorcery spells")
    void creatureIsNotOffered() {
        addGandalf();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addGandalf() {
        return addCreatureReady(player1, new GandalfPartyGuest());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
