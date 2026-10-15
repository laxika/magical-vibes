package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArchwayCommons;
import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.h.HeatedDebate;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({StoneriseSpirit.class, EagerFirstYear.class, ArchwayCommons.class, HeatedDebate.class})
class StoneriseSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any card from the graveyard and grants flying to the target creature")
    void exilesAnyCardAndGrantsFlying() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setGraveyard(player1, List.of(new ArchwayCommons()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = target.getId();

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Archway Commons");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Archway Commons"));

        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying granted by the ability expires at end of turn")
    void flyingExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setGraveyard(player1, List.of(new ArchwayCommons()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = target.getId();

        harness.activateAbility(player1, 0, null, targetId);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Eager First-Year").hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(findPermanent(player1, "Eager First-Year").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card in the graveyard")
    void cannotActivateWithoutGraveyardCard() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = target.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    void tappedSummoningSickSourceCanGrantFlyingToOpponentCreature() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new StoneriseSpirit());
        spirit.tap();
        spirit.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        harness.setGraveyard(player1, List.of(new EagerFirstYear()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Eager First-Year");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Eager First-Year"));
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(spirit.isTapped()).isTrue();
    }

    @Test
    void opponentsGraveyardCannotPayTheCost() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new ArchwayCommons()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");

        harness.assertInGraveyard(player2, "Archway Commons");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyThreeMana() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setGraveyard(player1, List.of(new ArchwayCommons()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> {
            harness.activateAbility(player1, 0, null, target.getId());
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.GraveyardExileCostChoice) {
                harness.handleGraveyardCardChosen(player1, 0);
            }
        }).isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Archway Commons");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetANoncreatureLand() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ArchwayCommons());
        harness.setGraveyard(player1, List.of(new EagerFirstYear()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Eager First-Year");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceDies() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new StoneriseSpirit());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setGraveyard(player1, List.of(new ArchwayCommons()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new HeatedDebate()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spirit.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stonerise Spirit");
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void removedTargetDoesNotRefundExileCost() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setGraveyard(player1, List.of(new ArchwayCommons()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new HeatedDebate()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Eager First-Year");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Archway Commons");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Archway Commons"));
    }
}
