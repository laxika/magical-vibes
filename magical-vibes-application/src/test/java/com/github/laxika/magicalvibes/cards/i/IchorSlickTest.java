package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IchorSlick.class, GrizzlyBears.class, Plains.class, RavensCrime.class, WallOfSwords.class})
class IchorSlickTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -3/-3 until end of turn")
    void givesTargetCreatureMinusThreeMinusThree() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isEqualTo(-3);
        assertThat(wall.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("The -3/-3 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, wall.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wall.getPowerModifier()).isZero();
        assertThat(wall.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ichor Slick");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Madness casts Ichor Slick for {3}{B}")
    void madnessCastsIchorSlick() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        discardIchorSlick();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ichor Slick");
    }

    private void discardIchorSlick() {
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }
}
