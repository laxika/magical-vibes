package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.a.AngelicShield;
import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DismantlingBlow.class, ChromaticSphere.class, AngelicShield.class,
        AlabasterLeech.class, Island.class})
class DismantlingBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact without kicker")
    void destroysArtifactWithoutKicker() {
        harness.addToBattlefield(player2, new ChromaticSphere());
        UUID targetId = harness.getPermanentId(player2, "Chromatic Sphere");
        harness.setHand(player1, List.of(new DismantlingBlow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Chromatic Sphere");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroys a target enchantment and draws two cards when kicked")
    void destroysEnchantmentAndDrawsTwoCardsWhenKicked() {
        harness.addToBattlefield(player2, new AngelicShield());
        UUID targetId = harness.getPermanentId(player2, "Angelic Shield");
        harness.setHand(player1, List.of(new DismantlingBlow()));
        harness.setLibrary(player1, List.of(new AlabasterLeech(), new AlabasterLeech()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castKickedInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Shield");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Alabaster Leech", "Alabaster Leech");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new DismantlingBlow()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
