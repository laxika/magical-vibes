package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OgreMarauder;
import com.github.laxika.magicalvibes.cards.r.RibbonsOfTheReikai;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlumberingTora.class, OgreMarauder.class, TeardropKami.class, RibbonsOfTheReikai.class})
class SlumberingToraTest extends BaseCardTest {

    @Test
    @DisplayName("Only Spirit and Arcane cards can be discarded to animate Slumbering Tora")
    void onlySpiritAndArcaneCardsAreValidDiscardChoices() {
        addReadyTora(player1);
        harness.setHand(player1, List.of(new OgreMarauder(), new TeardropKami(), new RibbonsOfTheReikai()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("Slumbering Tora becomes a Cat with power and toughness equal to the discarded card's mana value")
    void animationUsesDiscardedCardManaValue() {
        Permanent tora = addReadyTora(player1);
        TeardropKami discarded = new TeardropKami();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gqs.isArtifact(tora)).isTrue();
        assertThat(gqs.isCreature(gd, tora)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tora)).isEqualTo(discarded.getManaValue());
        assertThat(gqs.getEffectiveToughness(gd, tora)).isEqualTo(discarded.getManaValue());
        assertThat(gqs.hasEffectiveSubtype(gd, tora, CardSubtype.CAT)).isTrue();
        assertThat(tora.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each activation uses the mana value of its own discarded card")
    void separateActivationsUseTheirOwnDiscardedManaValues() {
        Permanent tora = addReadyTora(player1);
        TeardropKami firstDiscard = new TeardropKami();
        RibbonsOfTheReikai secondDiscard = new RibbonsOfTheReikai();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.withAutoStop(gd.currentStep, () -> {
            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);
        });

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, tora)).isEqualTo(secondDiscard.getManaValue());
        assertThat(gqs.getEffectiveToughness(gd, tora)).isEqualTo(secondDiscard.getManaValue());

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, tora)).isEqualTo(firstDiscard.getManaValue());
        assertThat(gqs.getEffectiveToughness(gd, tora)).isEqualTo(firstDiscard.getManaValue());
    }

    @Test
    @DisplayName("Slumbering Tora stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent tora = addReadyTora(player1);
        harness.setHand(player1, List.of(new RibbonsOfTheReikai()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, tora)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tora)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, tora, CardSubtype.CAT)).isFalse();
    }

    private Permanent addReadyTora(Player player) {
        return addCreatureReady(player, new SlumberingTora());
    }
}
