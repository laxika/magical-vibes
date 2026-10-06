package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
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

@CardUsed({ReconstructedThopter.class, Disfigure.class})
class ReconstructedThopterTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Reconstructed Thopter with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new ReconstructedThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Reconstructed Thopter");
        assertThat(thopter.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Reconstructed Thopter");

        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Reconstructed Thopter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Reconstructed Thopter"));
    }
    @Test
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new ReconstructedThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Reconstructed Thopter");
        harness.assertNotOnBattlefield(player1, "Reconstructed Thopter");
    }

    @Test
    void unearthRequiresTwoMana() {
        harness.setGraveyard(player1, List.of(new ReconstructedThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Reconstructed Thopter");
        harness.assertNotOnBattlefield(player1, "Reconstructed Thopter");
    }

    @Test
    void unearthedThopterIsExiledInsteadOfDying() {
        ReconstructedThopter card = new ReconstructedThopter();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent thopter = findPermanent(player1, "Reconstructed Thopter");
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, thopter.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reconstructed Thopter");
        harness.assertNotInGraveyard(player1, "Reconstructed Thopter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }
}
