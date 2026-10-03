package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackSunsTwilight.class, GiantSpider.class, GrizzlyBears.class, ColossalDreadmaw.class})
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

    @Test
    @DisplayName("The graveyard return cannot be declined when a qualifying card exists")
    void requiresGraveyardReturn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        cast(5, null);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot decline forced graveyard choice");
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns a creature whose mana value equals X")
    void returnsCreatureAtManaValueBoundary() {
        harness.setGraveyard(player1, List.of(new ColossalDreadmaw()));

        cast(6, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Colossal Dreadmaw").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Only creature cards from your own graveyard qualify")
    void excludesNoncreaturesAndOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new BlackSunsTwilight()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        cast(5, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("Shrinking a creature and returning another both happen at X=5")
    void shrinksAndReturnsInSameResolution() {
        Permanent target = addCreatureReady(player2, new ColossalDreadmaw());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        cast(5, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature reduced to zero toughness cannot be returned during the same resolution")
    void cannotReturnCreatureShrunkByThisSpell() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        cast(5, target.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
    }

    @Test
    @DisplayName("The shrink expires at end of turn")
    void shrinkExpiresAfterCleanup() {
        Permanent target = addCreatureReady(player2, new GiantSpider());

        cast(1, target.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("X=0 leaves the chosen creature unchanged")
    void zeroXDoesNotShrinkOrReturn() {
        Permanent target = addCreatureReady(player2, new GiantSpider());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        cast(0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An illegal creature target prevents the graveyard return")
    void illegalTargetPreventsReturn() {
        Permanent target = addCreatureReady(player1, new GiantSpider());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BlackSunsTwilight()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castInstant(player1, 0, 5, target.getId());

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new BlackSunsTwilight()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castInstant(player2, 0, 4, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Giant Spider");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
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
