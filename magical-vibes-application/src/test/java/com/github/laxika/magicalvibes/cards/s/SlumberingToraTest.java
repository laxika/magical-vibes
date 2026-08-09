package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoaningSpirit;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlumberingToraTest extends BaseCardTest {

    @Test
    @DisplayName("Only Spirit and Arcane cards can be discarded to animate Slumbering Tora")
    void onlySpiritAndArcaneCardsAreValidDiscardChoices() {
        addReadyTora(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new MoaningSpirit(), new ReachThroughMists()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("Slumbering Tora becomes a Cat with power and toughness equal to the discarded card's mana value")
    void animationUsesDiscardedCardManaValue() {
        Permanent tora = addReadyTora(player1);
        MoaningSpirit discarded = new MoaningSpirit();
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
        assertThat(tora.getTransientSubtypes()).contains(CardSubtype.CAT);
    }

    @Test
    @DisplayName("Slumbering Tora stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent tora = addReadyTora(player1);
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, tora)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tora)).isFalse();
        assertThat(tora.getTransientSubtypes()).isEmpty();
    }

    private Permanent addReadyTora(Player player) {
        Permanent permanent = new Permanent(new SlumberingTora());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
