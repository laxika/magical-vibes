package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeedSpark.class, AngelicChorus.class, Forest.class, Millstone.class})
class SeedSparkTest extends BaseCardTest {

    @Test
    void destroysArtifactAndCreatesSaprolingsIfGreenWasSpent() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new SeedSpark()));
        addManaWithGreen();

        UUID targetId = harness.getPermanentId(player2, "Millstone");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
    }

    @Test
    void destroysEnchantmentWithoutCreatingSaprolingsIfGreenWasNotSpent() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new SeedSpark()));
        addManaWithoutGreen();

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
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
