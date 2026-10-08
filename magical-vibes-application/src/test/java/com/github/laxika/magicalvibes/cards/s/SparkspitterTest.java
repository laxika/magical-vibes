package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
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

@CardUsed({Sparkspitter.class, FomoriNomad.class})
@DisplayName("Sparkspitter")
class SparkspitterTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card creates a 3/1 trampling hasty Spark Elemental")
    void discardingCardCreatesSparkElemental() {
        Permanent sparkspitter = addCreatureReady(player1, new Sparkspitter());
        harness.setHand(player1, List.of(new FomoriNomad()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(sparkspitter.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Permanent token = findPermanent(player1, "Spark Elemental");
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(token.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The Spark Elemental is sacrificed at the beginning of the next end step")
    void sparkElementalIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new Sparkspitter());
        harness.setHand(player1, List.of(new FomoriNomad()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spark Elemental");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Spark Elemental");
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent sparkspitter = addCreatureReady(player1, new Sparkspitter());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sparkspitter.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without red mana")
    void cannotActivateWithoutRedMana() {
        Permanent sparkspitter = addCreatureReady(player1, new Sparkspitter());
        harness.setHand(player1, List.of(new FomoriNomad()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sparkspitter.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent sparkspitter = harness.addToBattlefieldAndReturn(player1, new Sparkspitter());
        harness.setHand(player1, List.of(new FomoriNomad()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sparkspitter.isTapped()).isFalse();
        harness.assertInHand(player1, "Fomori Nomad");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Spark Elemental");
    }

    @Test
    @DisplayName("A tapped Sparkspitter cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent sparkspitter = addCreatureReady(player1, new Sparkspitter());
        sparkspitter.tap();
        harness.setHand(player1, List.of(new FomoriNomad()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Fomori Nomad");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Spark Elemental");
    }

    @Test
    @DisplayName("A token created during an end step waits until the following end step")
    void tokenCreatedDuringEndStepSurvivesUntilOpponentsEndStep() {
        addCreatureReady(player1, new Sparkspitter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new FomoriNomad()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Fomori Nomad");
        harness.assertNotOnBattlefield(player1, "Spark Elemental");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spark Elemental");
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Spark Elemental");
        harness.assertOnBattlefield(player1, "Sparkspitter");
    }
}
