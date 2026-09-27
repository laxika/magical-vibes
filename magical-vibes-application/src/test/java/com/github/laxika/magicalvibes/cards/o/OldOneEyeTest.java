package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({OldOneEye.class, GrizzlyBears.class, Forest.class})
class OldOneEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your other creatures trample and creates a 5/5 Tyranid on entry")
    void grantsTrampleAndCreatesTyranid() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        castOldOneEye();

        Permanent token = findPermanent(player1, "Tyranid");
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Fast Healing returns it after discarding two cards")
    void fastHealingReturnsItToHand() {
        OldOneEye oldOneEye = new OldOneEye();
        harness.setGraveyard(player1, List.of(oldOneEye));
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToPrecombatMain(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(oldOneEye.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(oldOneEye.getId()));
    }

    @Test
    @DisplayName("Fast Healing is optional")
    void fastHealingCanBeDeclined() {
        OldOneEye oldOneEye = new OldOneEye();
        harness.setGraveyard(player1, List.of(oldOneEye));
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToPrecombatMain(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldOneEye);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(oldOneEye);
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
