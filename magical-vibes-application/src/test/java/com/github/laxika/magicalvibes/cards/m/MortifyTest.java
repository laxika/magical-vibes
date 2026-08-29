package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MortifyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a targeted creature")
    void destroysCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMortifyAt(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a targeted enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        castMortifyAt(harness.getPermanentId(player2, "Angelic Chorus"));

        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonenchantment permanent")
    void cannotTargetForest() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana();

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment");
    }

    private void castMortifyAt(UUID targetId) {
        harness.setHand(player1, List.of(new Mortify()));
        addMortifyMana();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMortifyMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
