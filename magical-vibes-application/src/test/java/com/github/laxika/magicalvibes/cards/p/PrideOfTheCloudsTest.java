package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

@CardUsed({PrideOfTheClouds.class, SuntailHawk.class, GrizzlyBears.class})
class PrideOfTheCloudsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other creature with flying")
    void boostsForOtherFlyingCreatures() {
        Permanent pride = harness.addToBattlefieldAndReturn(player1, new PrideOfTheClouds());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, pride)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pride)).isEqualTo(3);
    }

    @Test
    @DisplayName("Forecast creates a multicolored Bird and keeps the card in hand")
    void forecastCreatesBirdAndKeepsSourceInHand() {
        PrideOfTheClouds pride = new PrideOfTheClouds();
        harness.setHand(player1, List.of(pride));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pride);
        harness.passBothPriorities();

        Permanent bird = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
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
}
