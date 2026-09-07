package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Sparkspitter.class, GrizzlyBears.class})
@DisplayName("Sparkspitter")
class SparkspitterTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card creates a 3/1 trampling hasty Spark Elemental")
    void discardingCardCreatesSparkElemental() {
        Permanent sparkspitter = harness.addToBattlefieldAndReturn(player1, new Sparkspitter());
        sparkspitter.setSummoningSick(false);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Permanent token = findPermanent(player1, "Spark Elemental");
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(token.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The Spark Elemental is sacrificed at the beginning of the next end step")
    void sparkElementalIsSacrificedAtNextEndStep() {
        Permanent sparkspitter = harness.addToBattlefieldAndReturn(player1, new Sparkspitter());
        sparkspitter.setSummoningSick(false);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spark Elemental");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spark Elemental");
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent sparkspitter = harness.addToBattlefieldAndReturn(player1, new Sparkspitter());
        sparkspitter.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }
}
