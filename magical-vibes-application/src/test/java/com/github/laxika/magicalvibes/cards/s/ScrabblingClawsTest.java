package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrabblingClawsTest extends BaseCardTest {

    @Test
    void tapsToMakeTargetPlayerExileCardFromTheirGraveyard() {
        harness.addToBattlefield(player1, new ScrabblingClaws());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
    }

    @Test
    void sacrificesItselfExilesTargetCardAndDraws() {
        Card graveyardCard = new GrizzlyBears();
        Card libraryCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new ScrabblingClaws());
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(libraryCard);
        harness.assertNotOnBattlefield(player1, "Scrabbling Claws");
    }

    @Test
    void cannotActivateSacrificeAbilityWithoutGraveyardTarget() {
        harness.addToBattlefield(player1, new ScrabblingClaws());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
