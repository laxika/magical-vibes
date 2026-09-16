package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TirelessTribe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaywardAngel.class, TirelessTribe.class, Plains.class})
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
    @DisplayName("Has flying and vigilance")
    void hasPrintedKeywords() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());

        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        fillGraveyard(player2, 7);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasColor(gd, angel, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Threshold bonuses disappear when its controller's graveyard drops below seven cards")
    void thresholdBonusDisappearsBelowSevenCards() {
        fillGraveyard(player1, 7);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.BLACK)).isTrue();

        fillGraveyard(player1, 6);

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
        Permanent tribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(angel.getId(), tribe.getId()).doesNotContain(plains.getId());
        harness.handlePermanentChosen(player1, tribe.getId());

        harness.assertOnBattlefield(player1, "Wayward Angel");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Tireless Tribe");
        harness.assertInGraveyard(player1, "Tireless Tribe");
    }

    @Test
    @DisplayName("Threshold upkeep trigger is absent below seven cards")
    void noThresholdUpkeepTrigger() {
        fillGraveyard(player1, 6);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());
        Permanent tribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(angel, tribe);
    }

    @Test
    @DisplayName("Threshold upkeep trigger does not fire during an opponent's upkeep")
    void noThresholdUpkeepTriggerDuringOpponentsUpkeep() {
        fillGraveyard(player1, 7);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());
        Permanent tribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(angel, tribe);
    }

    @Test
    @DisplayName("An upkeep trigger still resolves after threshold ends")
    void upkeepTriggerResolvesAfterThresholdEnds() {
        fillGraveyard(player1, 7);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WaywardAngel());
        Permanent tribe = harness.addToBattlefieldAndReturn(player1, new TirelessTribe());

        advanceToUpkeep(player1);
        fillGraveyard(player1, 6);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(angel.getId(), tribe.getId());
        harness.handlePermanentChosen(player1, tribe.getId());

        harness.assertOnBattlefield(player1, "Wayward Angel");
        harness.assertNotOnBattlefield(player1, "Tireless Tribe");
        harness.assertInGraveyard(player1, "Tireless Tribe");
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new TirelessTribe());
        }
        harness.setGraveyard(player, cards);
    }
}
