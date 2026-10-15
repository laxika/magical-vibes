package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrandCrescendo.class, GrizzlyBears.class})
class GrandCrescendoTest extends BaseCardTest {

    @Test
    @DisplayName("Creates X green and white Citizen tokens and grants indestructible to your creatures")
    void createsCitizensAndGrantsIndestructible() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(player1, 2);

        List<Permanent> citizens = findPermanents(player1, "Citizen");
        assertThat(citizens).hasSize(2);
        assertThat(citizens).allSatisfy(citizen -> {
            assertThat(citizen.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(citizen.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
            assertThat(gqs.hasKeyword(gd, citizen, Keyword.INDESTRUCTIBLE)).isTrue();
        });
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(player1, 0);

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Zero X creates no tokens but still protects existing creatures")
    void zeroXStillProtectsExistingCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(player1, 0);

        assertThat(findPermanents(player1, "Citizen")).isEmpty();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @CardUsed({GrandCrescendo.class})
    @DisplayName("Can resolve with zero X and no creatures")
    void zeroXWithEmptyBattlefield() {
        cast(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain indestructible")
    void laterCreaturesDoNotGainIndestructible() {
        cast(player1, 1);
        Permanent laterBear = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Citizen"), Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @CardUsed({GrandCrescendo.class})
    @DisplayName("Created Citizens are untapped 1/1 creatures")
    void createsUntappedOneOneCreatures() {
        cast(player1, 1);

        Permanent citizen = findPermanent(player1, "Citizen");
        assertThat(gqs.isCreature(gd, citizen)).isTrue();
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(1);
        assertThat(citizen.isTapped()).isFalse();
    }
    private void cast(Player player, int xValue) {
        harness.setHand(player, List.of(new GrandCrescendo()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, xValue);
        harness.castInstant(player, 0, xValue, null);
        harness.passBothPriorities();
    }
}
