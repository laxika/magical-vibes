package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AugurOfSkulls;
import com.github.laxika.magicalvibes.cards.t.Tombstalker;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
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

@CardUsed({IchorSlick.class, AugurOfSkulls.class, Tombstalker.class, ZoeticCavern.class})
class IchorSlickTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -3/-3 until end of turn")
    void givesTargetCreatureMinusThreeMinusThree() {
        Permanent tombstalker = harness.addToBattlefieldAndReturn(player2, new Tombstalker());
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, tombstalker.getId());

        assertThat(tombstalker.getPowerModifier()).isEqualTo(-3);
        assertThat(tombstalker.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("The -3/-3 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent tombstalker = harness.addToBattlefieldAndReturn(player2, new Tombstalker());
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, tombstalker.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tombstalker.getPowerModifier()).isZero();
        assertThat(tombstalker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ZoeticCavern());
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new IchorSlick()));
        harness.setLibrary(player1, List.of(new AugurOfSkulls()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ichor Slick");
        harness.assertInHand(player1, "Augur of Skulls");
    }

    @Test
    @DisplayName("Madness casts Ichor Slick for {3}{B}")
    void madnessCastsIchorSlick() {
        discardIchorSlick();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AugurOfSkulls());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Augur of Skulls");
        harness.assertInGraveyard(player1, "Ichor Slick");
    }

    @Test
    @DisplayName("Declining madness puts Ichor Slick into its owner's graveyard")
    void decliningMadnessPutsCardIntoGraveyard() {
        discardIchorSlick();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ichor Slick");
    }

    private void discardIchorSlick() {
        harness.setHand(player1, List.of(new IchorSlick(), new ZoeticCavern()));
        harness.addToBattlefield(player2, new AugurOfSkulls());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player2, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
    }
}
