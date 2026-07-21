package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AspectOfWolf;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NissasDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a Forest without drawing")
    void destroysForestWithoutDraw() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroys a green enchantment without drawing")
    void destroysGreenEnchantmentWithoutDraw() {
        Permanent aura = new Permanent(new AspectOfWolf());
        gd.playerBattlefields.get(player2.getId()).add(aura);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aspect of Wolf");
        harness.assertInGraveyard(player2, "Aspect of Wolf");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroying a Nissa planeswalker draws a card")
    void destroyingNissaDrawsACard() {
        Permanent nissa = new Permanent(new NissaStewardOfElements());
        nissa.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(nissa);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, nissa.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nissa, Steward of Elements");
        harness.assertInGraveyard(player2, "Nissa, Steward of Elements");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a non-Forest creature")
    void cannotTargetNonForestCreature() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forest, green enchantment, or green planeswalker");
    }
}
