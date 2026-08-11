package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaywardAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3, trample, and black with seven cards in controller's graveyard")
    void thresholdBonus() {
        fillGraveyard(player1, 7);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, angel)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Does not get threshold abilities with only six cards")
    void noThresholdBonus() {
        fillGraveyard(player1, 6);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasColor(gd, angel, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Threshold upkeep trigger sacrifices a creature")
    void thresholdUpkeepTriggerSacrificesCreature() {
        fillGraveyard(player1, 7);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(angel.getId(), bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(angel)
                .doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Threshold upkeep trigger is absent below seven cards")
    void noThresholdUpkeepTrigger() {
        fillGraveyard(player1, 6);
        harness.addToBattlefield(player1, new WaywardAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }
}
