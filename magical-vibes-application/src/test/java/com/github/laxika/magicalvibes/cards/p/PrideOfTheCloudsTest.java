package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.g.GuardianOfTheGuildpact;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrideOfTheClouds.class, AzoriusFirstWing.class, GuardianOfTheGuildpact.class})
class PrideOfTheCloudsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other creature with flying")
    void boostsForOtherFlyingCreatures() {
        Permanent pride = harness.addToBattlefieldAndReturn(player1, new PrideOfTheClouds());
        harness.addToBattlefield(player1, new AzoriusFirstWing());
        harness.addToBattlefield(player2, new AzoriusFirstWing());
        harness.addToBattlefield(player1, new GuardianOfTheGuildpact());

        assertThat(gqs.getEffectivePower(gd, pride)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pride)).isEqualTo(3);
    }

    @Test
    @DisplayName("Forecast creates a multicolored Bird and keeps the card in hand")
    void forecastCreatesBirdAndKeepsSourceInHand() {
        PrideOfTheClouds pride = new PrideOfTheClouds();
        harness.setHand(player1, List.of(pride));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pride);
        harness.passBothPriorities();

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(bird.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Forecast can be activated only once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        harness.setHand(player1, List.of(new PrideOfTheClouds()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast cannot be activated outside its controller's upkeep")
    void forecastRequiresUpkeep() {
        harness.setHand(player1, List.of(new PrideOfTheClouds()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
    }

    @Test
    @DisplayName("Does not count itself as another flying creature")
    void doesNotCountItself() {
        Permanent pride = harness.addToBattlefieldAndReturn(player1, new PrideOfTheClouds());

        assertThat(gqs.getEffectivePower(gd, pride)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pride)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each copy in hand can forecast once in the same upkeep")
    void separateCopiesCanForecast() {
        harness.setHand(player1, List.of(new PrideOfTheClouds(), new PrideOfTheClouds()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 1, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Forecast cannot be activated in the opponent's upkeep")
    void forecastRejectsOpponentsUpkeep() {
        harness.setHand(player1, List.of(new PrideOfTheClouds()));
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your upkeep");
    }

    @Test
    @DisplayName("Forecast keeps its source revealed until the upkeep ends")
    void forecastKeepsSourceRevealedDuringUpkeep() {
        harness.setHand(player1, List.of(new PrideOfTheClouds()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.clearMessages();
        harness.publishState();
        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\""))
                .anyMatch(message -> message.contains("\"opponentHand\":[{")
                        && message.contains("Pride of the Clouds"));

        harness.passUntil(TurnStep.DRAW);
        harness.clearMessages();
        harness.publishState();
        assertThat(harness.getConn2().getMessagesContaining("\"opponentHand\":[]"))
                .isNotEmpty();
    }
}
