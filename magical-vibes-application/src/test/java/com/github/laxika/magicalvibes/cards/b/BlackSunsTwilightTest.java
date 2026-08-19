package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlackSunsTwilightTest extends BaseCardTest {

    @Test
    @DisplayName("Gives up to one target creature -X/-X until end of turn")
    void shrinksTargetCreature() {
        Permanent target = addCreatureReady(player2, new GiantSpider());

        cast(1, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("At X=5, returns a qualifying creature from the graveyard tapped")
    void returnsCreatureAtThreshold() {
        var tooExpensive = new ColossalDreadmaw();
        var creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(tooExpensive, creature));

        cast(5, null);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleGraveyardCardChosen(player1, 1);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Does not return a creature below X=5")
    void skipsGraveyardReturnBelowThreshold() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        cast(4, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast(int xValue, UUID targetId) {
        harness.setHand(player1, List.of(new BlackSunsTwilight()));
        harness.addMana(player1, ManaColor.BLACK, xValue + 1);
        if (targetId == null) {
            harness.castInstantForX(player1, 0, xValue, List.of());
        } else {
            harness.castInstant(player1, 0, xValue, targetId);
        }
        harness.passBothPriorities();
    }
}
