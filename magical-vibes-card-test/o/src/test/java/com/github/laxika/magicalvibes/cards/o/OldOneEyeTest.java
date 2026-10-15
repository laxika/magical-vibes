package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SicarianInfiltrator;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldOneEye.class, SicarianInfiltrator.class, Forest.class})
class OldOneEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your other creatures trample and creates a 5/5 Tyranid on entry")
    void grantsTrampleAndCreatesTyranid() {
        Permanent ownCreature = addCreatureReady(player1, new SicarianInfiltrator());
        Permanent opponentCreature = addCreatureReady(player2, new SicarianInfiltrator());
        castOldOneEye();

        Permanent token = findPermanent(player1, "Tyranid");
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Fast Healing returns it after discarding two cards")
    void fastHealingReturnsItToHand() {
        OldOneEye oldOneEye = new OldOneEye();
        harness.setGraveyard(player1, List.of(oldOneEye));
        harness.setHand(player1, List.of(new SicarianInfiltrator(), new Forest()));

        advanceToPrecombatMain(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
            harness.handleCardChosen(player1, 0);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(oldOneEye);
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(oldOneEye);
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    @DisplayName("Fast Healing is optional")
    void fastHealingCanBeDeclined() {
        OldOneEye oldOneEye = new OldOneEye();
        harness.setGraveyard(player1, List.of(oldOneEye));
        harness.setHand(player1, List.of(new SicarianInfiltrator(), new Forest()));

        advanceToPrecombatMain(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldOneEye);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(oldOneEye);
    }

    @Test
    @DisplayName("Fast Healing cannot discard a single card when two are required")
    void cannotPayFastHealingWithOnlyOneCard() {
        OldOneEye oldOneEye = new OldOneEye();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(oldOneEye));
        harness.setHand(player1, List.of(forest));

        advanceToPrecombatMain(player1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, true);
            }
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldOneEye);
        });
    }

    @Test
    @DisplayName("Fast Healing does not return the card with an empty hand")
    void fastHealingCannotBePaidWithEmptyHand() {
        OldOneEye oldOneEye = new OldOneEye();
        harness.setGraveyard(player1, List.of(oldOneEye));
        harness.setHand(player1, List.of());

        advanceToPrecombatMain(player1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, true);
            }
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldOneEye);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        });
    }

    @Test
    @DisplayName("Fast Healing does not trigger during the opponent's first main phase")
    void fastHealingDoesNotTriggerOnOpponentsTurn() {
        OldOneEye oldOneEye = new OldOneEye();
        harness.setGraveyard(player1, List.of(oldOneEye));
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        advanceToPrecombatMain(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldOneEye);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Fast Healing does not trigger while Old One Eye is on the battlefield")
    void fastHealingOnlyTriggersFromGraveyard() {
        addCreatureReady(player1, new OldOneEye());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        advanceToPrecombatMain(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void castOldOneEye() {
        harness.setHand(player1, List.of(new OldOneEye()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToPrecombatMain(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }
}
