package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntimidationCampaign.class, Forest.class, Shock.class})
class IntimidationCampaignTest extends BaseCardTest {

    @Test
    @DisplayName("Entering makes each opponent lose 1 life, gains 1 life, and draws a card")
    void enterAbility() {
        Forest drawnCard = new Forest();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new IntimidationCampaign()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Accepting the crime trigger returns Intimidation Campaign to its owner's hand")
    void crimeTriggerReturnsItToHandWhenAccepted() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new IntimidationCampaign());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(campaign);
        assertThat(gd.playerHands.get(player1.getId())).contains(campaign.getCard());
    }

    @Test
    @DisplayName("Declining the crime trigger leaves Intimidation Campaign on the battlefield")
    void crimeTriggerLeavesItOnBattlefieldWhenDeclined() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new IntimidationCampaign());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(campaign);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(campaign.getCard());
    }

    @Test
    @DisplayName("Targeting yourself does not trigger the campaign")
    void targetingYourselfIsNotACrime() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new IntimidationCampaign());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(campaign);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(campaign.getCard());
    }

    @Test
    @DisplayName("An opponent's crime does not trigger your campaign")
    void opponentsCrimeDoesNotTriggerCampaign() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new IntimidationCampaign());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(campaign);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(campaign.getCard());
    }

    @Test
    @DisplayName("Declining one crime does not prevent returning the campaign for a later crime")
    void triggersForEachCrimeInTheSameTurn() {
        Permanent campaign = harness.addToBattlefieldAndReturn(player1, new IntimidationCampaign());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(campaign);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(campaign.getCard());
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }
}
