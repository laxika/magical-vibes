package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightLuminary.class})
class KnightLuminaryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 white Human Soldier token")
    void etbCreatesHumanSoldierToken() {
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token).isNotNull();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Human Soldier")).isZero();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Knight Luminary");
    }

    @Test
    @DisplayName("Warp casts Knight Luminary for {1}{W} and exiles it at the next end step")
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        KnightLuminary knight = new KnightLuminary();
        harness.setHand(player1, List.of(knight));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(knight.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Knight Luminary");
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("A warped Knight can be cast on a later turn for its normal cost and stays in play")
    void castFromExileCreatesAnotherTokenAndDoesNotExileAgain() {
        harness.setLibrary(player1, List.of(new KnightLuminary(), new KnightLuminary()));
        harness.setLibrary(player2, List.of(new KnightLuminary(), new KnightLuminary()));
        KnightLuminary knight = new KnightLuminary();
        harness.setHand(player1, List.of(knight));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, knight.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(knight.getId())).isNull();
        harness.assertOnBattlefield(player1, "Knight Luminary");
        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Knight Luminary");
    }
}
