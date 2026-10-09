package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CombatCourier.class})
class CombatCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Combat Courier draws a card")
    void sacrificeAbilityDrawsCard() {
        harness.addToBattlefield(player1, new CombatCourier());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Combat Courier");
        harness.assertInGraveyard(player1, "Combat Courier");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Unearth returns Combat Courier with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new CombatCourier()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent courier = findPermanent(player1, "Combat Courier");
        assertThat(gqs.hasKeyword(gd, courier, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Combat Courier");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Combat Courier");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Combat Courier"));
    }

    @Test
    void sacrificingUnearthedCourierExilesItAndStillDraws() {
        CombatCourier card = new CombatCourier();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Combat Courier");
        harness.assertNotInGraveyard(player1, "Combat Courier");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void tappedCourierCanBeSacrificed() {
        harness.addToBattlefield(player1, new CombatCourier());
        findPermanent(player1, "Combat Courier").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Combat Courier");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new CombatCourier()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Combat Courier");
        harness.assertNotOnBattlefield(player1, "Combat Courier");
    }

    @Test
    void unearthRequiresBlueMana() {
        harness.setGraveyard(player1, List.of(new CombatCourier()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Combat Courier");
        harness.assertNotOnBattlefield(player1, "Combat Courier");
    }
}
