package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FestivalOfEmbers.class, Shock.class, GrizzlyBears.class})
class FestivalOfEmbersTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a graveyard instant during its controller's turn and pays 1 life")
    void castsInstantFromGraveyardAndPaysLife() {
        harness.addToBattlefield(player1, new FestivalOfEmbers());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Cannot use the graveyard-cast permission during another player's turn")
    void cannotCastDuringAnotherPlayersTurn() {
        harness.addToBattlefield(player1, new FestivalOfEmbers());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(harness.getCastingPermissionService()
                .canCastViaFilteredGraveyardPermission(gd, player1.getId(), shock)).isFalse();
    }

    @Test
    @DisplayName("Does not allow permanent cards to be cast from the graveyard")
    void doesNotAllowPermanentCardsFromGraveyard() {
        harness.addToBattlefield(player1, new FestivalOfEmbers());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Sacrifice ability keeps the enchantment until resolution and then exiles it")
    void sacrificeAbilityExilesItOnResolution() {
        harness.addToBattlefield(player1, new FestivalOfEmbers());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertOnBattlefield(player1, "Festival of Embers");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Festival of Embers");
        harness.assertNotInGraveyard(player1, "Festival of Embers");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Festival of Embers"));
    }
}
