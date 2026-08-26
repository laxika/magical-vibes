package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GlintEyeNephilim.class, Forest.class, Island.class, GrizzlyBears.class})
class GlintEyeNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws that many cards")
    void combatDamageDrawsEqualToDamageDealt() {
        Card firstDraw = new Forest();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addReadyNephilim();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discarding a card gives Glint-Eye Nephilim +1/+1 until end of turn")
    void discardAbilityBoostsUntilEndOfTurn() {
        Permanent nephilim = addReadyNephilim();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nephilim)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nephilim)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nephilim)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nephilim)).isEqualTo(2);
    }

    private Permanent addReadyNephilim() {
        return addCreatureReady(player1, new GlintEyeNephilim());
    }
}
