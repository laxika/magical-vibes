package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NocturnalHunger.class, GrizzlyBears.class, FountainOfYouth.class})
class NocturnalHungerTest extends BaseCardTest {

    @Test
    void withoutGiftDestroysCreatureAndLosesTwoLife() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        cast(bear, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    void promisedGiftCreatesFoodAndSkipsLifeLoss() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        cast(bear, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    void canTargetOnlyAcreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new NocturnalHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, artifact.getId(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void giftedFoodCanBeSacrificedForThreeLifeImmediately() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        cast(bear, true);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 23);
    }

    @Test
    void canDestroyOwnCreatureWhileOpponentReceivesGift() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        cast(bear, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Food");
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void illegalTargetPreventsBothGiftAndLifeLoss(boolean giftPromised) {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new NocturnalHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstantWithGift(player1, 0, bear.getId(), giftPromised);

        harness.setHand(player2, List.of(new NocturnalHunger()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertInGraveyard(player1, "Nocturnal Hunger");
    }
    private void cast(Permanent target, boolean giftPromised) {
        harness.setHand(player1, List.of(new NocturnalHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstantWithGift(player1, 0, target.getId(), giftPromised);
        harness.passBothPriorities();
    }
}
