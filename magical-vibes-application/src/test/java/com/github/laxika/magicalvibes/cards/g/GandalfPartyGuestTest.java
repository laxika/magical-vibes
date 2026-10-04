package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzamiLadyOfScrolls;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.m.MerfolkSecretkeeper;
import com.github.laxika.magicalvibes.cards.p.PeerThroughDepths;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfPartyGuest.class, AzamiLadyOfScrolls.class, CounselOfTheSoratami.class,
        GrizzlyBears.class, MerfolkSecretkeeper.class, PeerThroughDepths.class})
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

    @Test
    @DisplayName("An instant at the mana-value limit can be cast without mana")
    void castsInstantAtLimitWithoutMana() {
        addGandalf();
        PeerThroughDepths spell = new PeerThroughDepths();
        harness.setHand(player1, List.of(spell));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The player may decline the free spell")
    void mayDecline() {
        addGandalf();
        PeerThroughDepths spell = new PeerThroughDepths();
        harness.setHand(player1, List.of(spell));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        addGandalf();
        PeerThroughDepths spell = new PeerThroughDepths();
        harness.setHand(player1, List.of(spell));

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("An opponent's legendary Wizard does not increase the limit")
    void opponentsLegendaryWizardDoesNotIncreaseLimit() {
        addGandalf();
        addCreatureReady(player2, new AzamiLadyOfScrolls());
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(spell));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("A nonlegendary Wizard does not increase the limit")
    void nonlegendaryWizardDoesNotIncreaseLimit() {
        addGandalf();
        addCreatureReady(player1, new MerfolkSecretkeeper());
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(spell));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Only one spell may be cast by each trigger")
    void castsOnlyOneSpell() {
        addGandalf();
        addCreatureReady(player1, new AzamiLadyOfScrolls());
        CounselOfTheSoratami first = new CounselOfTheSoratami();
        PeerThroughDepths second = new PeerThroughDepths();
        harness.setHand(player1, List.of(first, second));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An eligible Adventure sorcery can be offered from hand")
    void offersAdventureSorcery() {
        addGandalf();
        MerfolkSecretkeeper spell = new MerfolkSecretkeeper();
        harness.setHand(player1, List.of(spell));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    private void addGandalf() {
        addCreatureReady(player1, new GandalfPartyGuest());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }
}
