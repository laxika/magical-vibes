package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GolgariGermination;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeedSpark.class, BorosSignet.class, GolgariGermination.class, Forest.class})
class SeedSparkTest extends BaseCardTest {

    @Test
    void destroysArtifactAndCreatesSaprolingsIfGreenWasSpent() {
        harness.addToBattlefield(player2, new BorosSignet());
        harness.setHand(player1, List.of(new SeedSpark()));
        addManaWithGreen();

        UUID targetId = harness.getPermanentId(player2, "Boros Signet");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Boros Signet");
        harness.assertInGraveyard(player2, "Boros Signet");
        assertThat(findPermanents(player1, "Saproling")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getColors()).containsExactly(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    void destroysEnchantmentWithoutCreatingSaprolingsIfGreenWasNotSpent() {
        harness.addToBattlefield(player2, new GolgariGermination());
        harness.setHand(player1, List.of(new SeedSpark()));
        addManaWithoutGreen();

        UUID targetId = harness.getPermanentId(player2, "Golgari Germination");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Golgari Germination");
        harness.assertInGraveyard(player2, "Golgari Germination");
        assertThat(countPermanents(player1, "Saproling")).isZero();
    }

    @Test
    void cannotTargetALand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SeedSpark()));
        addManaWithoutGreen();

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    void createsOnlyTwoSaprolingsWhenMultipleGreenManaWasSpentOnOwnEnchantment() {
        harness.addToBattlefield(player1, new GolgariGermination());
        harness.setHand(player1, List.of(new SeedSpark()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Golgari Germination");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Golgari Germination");
        harness.assertNotOnBattlefield(player1, "Golgari Germination");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
        assertThat(countPermanents(player2, "Saproling")).isZero();
    }

    @Test
    void greenManaAddedAfterCastingDoesNotCreateSaprolings() {
        harness.addToBattlefield(player2, new BorosSignet());
        harness.setHand(player1, List.of(new SeedSpark()));
        addManaWithoutGreen();
        UUID targetId = harness.getPermanentId(player2, "Boros Signet");
        harness.castInstant(player1, 0, targetId);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boros Signet");
        assertThat(countPermanents(player1, "Saproling")).isZero();
    }

    @Test
    void createsNoSaprolingsWhenTargetIsDestroyedInResponse() {
        harness.addToBattlefield(player2, new BorosSignet());
        harness.setHand(player1, List.of(new SeedSpark()));
        addManaWithGreen();
        UUID targetId = harness.getPermanentId(player2, "Boros Signet");
        harness.castInstant(player1, 0, targetId);

        harness.setHand(player2, List.of(new SeedSpark()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boros Signet");
        harness.assertInGraveyard(player1, "Seed Spark");
        harness.assertInGraveyard(player2, "Seed Spark");
        assertThat(countPermanents(player1, "Saproling")).isZero();
        assertThat(countPermanents(player2, "Saproling")).isZero();
    }

    private void addManaWithGreen() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addManaWithoutGreen() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
