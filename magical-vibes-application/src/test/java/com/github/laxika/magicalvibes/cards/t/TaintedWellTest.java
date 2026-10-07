package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.d.DismantlingBlow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaintedWell.class, Forest.class, AlabasterLeech.class, DismantlingBlow.class})
class TaintedWellTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Tainted Well attaches to a land and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new TaintedWell()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> forest.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Enchanted land becomes a Swamp")
    void enchantedLandBecomesSwamp() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TaintedWell());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Tainted Well cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new Forest());
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new AlabasterLeech());
        harness.setHand(player1, List.of(new TaintedWell()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonland.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Tainted Well can enchant an opponent's land")
    void canEnchantOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TaintedWell()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> forest.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("The Aura's controller draws when enchanting an opponent's land")
    void auraControllerDrawsForOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TaintedWell()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new AlabasterLeech()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Alabaster Leech");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Removing the Aura restores the land and does not stop its pending draw trigger")
    void removingAuraRestoresLandAndStillDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new TaintedWell()));
        harness.setHand(player2, List.of(new DismantlingBlow()));
        harness.setLibrary(player1, List.of(new AlabasterLeech()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> forest.getId().equals(permanent.getAttachedTo()))
                .findFirst().orElseThrow();
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tainted Well");
        harness.assertInHand(player1, "Alabaster Leech");
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
