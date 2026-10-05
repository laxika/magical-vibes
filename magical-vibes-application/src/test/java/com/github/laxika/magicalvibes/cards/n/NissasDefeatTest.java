package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SandwurmConvergence;
import com.github.laxika.magicalvibes.cards.s.SamutTheTested;
import com.github.laxika.magicalvibes.cards.s.ShelteredThicket;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.cards.f.FrayingSanity;
import com.github.laxika.magicalvibes.cards.h.HashepOasis;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissasDefeat.class, SandwurmConvergence.class, Forest.class, FeralProwler.class,
        NissaGenesisMage.class, FrayingSanity.class, HashepOasis.class, NicolBolasTheDeceiver.class, SamutTheTested.class, ShelteredThicket.class})
class NissasDefeatTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a Forest without drawing")
    void destroysForestWithoutDraw() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player1, List.of(new FeralProwler()));

        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, forest.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroys a green enchantment without drawing")
    void destroysGreenEnchantmentWithoutDraw() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SandwurmConvergence());
        harness.setLibrary(player1, List.of(new FeralProwler()));

        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Sandwurm Convergence");
        harness.assertInGraveyard(player2, "Sandwurm Convergence");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroying a Nissa planeswalker draws a card")
    void destroyingNissaDrawsACard() {
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaGenesisMage());
        nissa.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new FeralProwler()));

        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, nissa.getId());

        harness.assertNotOnBattlefield(player2, "Nissa, Genesis Mage");
        harness.assertInGraveyard(player2, "Nissa, Genesis Mage");
        harness.assertInHand(player1, "Feral Prowler");
    }

    @Test
    @DisplayName("Cannot target a non-Forest creature")
    void cannotTargetNonForestCreature() {
        harness.addToBattlefield(player2, new FeralProwler());

        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID prowlerId = harness.getPermanentId(player2, "Feral Prowler");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, prowlerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forest, green enchantment, or green planeswalker");
    }

    @Test
    void drawsExactlyOneCardWhenNissaIsIndestructible() {
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaGenesisMage());
        nissa.setCounterCount(CounterType.LOYALTY, 5);
        nissa.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setLibrary(player1, List.of(new FeralProwler(), new Forest()));
        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, nissa.getId());

        harness.assertOnBattlefield(player2, "Nissa, Genesis Mage");
        harness.assertNotInGraveyard(player2, "Nissa, Genesis Mage");
        harness.assertInHand(player1, "Feral Prowler");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenNissaLeavesBeforeResolution() {
        Permanent nissa = harness.addToBattlefieldAndReturn(player2, new NissaGenesisMage());
        nissa.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, nissa.getId());
        gd.playerBattlefields.get(player2.getId()).remove(nissa);
        gd.playerHands.get(player2.getId()).add(nissa.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Nissa's Defeat");
    }

    @Test
    void canDestroyOwnNissaAndDraw() {
        Permanent nissa = harness.addToBattlefieldAndReturn(player1, new NissaGenesisMage());
        nissa.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, nissa.getId());

        harness.assertInGraveyard(player1, "Nissa, Genesis Mage");
        harness.assertNotOnBattlefield(player1, "Nissa, Genesis Mage");
        harness.assertInHand(player1, "Feral Prowler");
    }

    @Test
    void cannotTargetGreenManaLandWithoutForestSubtype() {
        Permanent oasis = harness.addToBattlefieldAndReturn(player2, new HashepOasis());
        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, oasis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forest, green enchantment, or green planeswalker");
    }

    @Test
    void cannotTargetNonGreenEnchantment() {
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new FrayingSanity());
        curse.setAttachedTo(player1.getId());
        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, curse.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forest, green enchantment, or green planeswalker");
    }

    @Test
    void cannotTargetNonGreenPlaneswalker() {
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasTheDeceiver());
        bolas.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bolas.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Forest, green enchantment, or green planeswalker");
    }

    @Test
    void destroysMulticoloredGreenNonNissaPlaneswalkerWithoutDrawing() {
        Permanent samut = harness.addToBattlefieldAndReturn(player2, new SamutTheTested());
        samut.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, samut.getId());

        harness.assertNotOnBattlefield(player2, "Samut, the Tested");
        harness.assertInGraveyard(player2, "Samut, the Tested");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void destroysNonbasicForestWithoutDrawing() {
        Permanent thicket = harness.addToBattlefieldAndReturn(player2, new ShelteredThicket());
        harness.setLibrary(player1, List.of(new FeralProwler()));
        harness.setHand(player1, List.of(new NissasDefeat()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, thicket.getId());

        harness.assertNotOnBattlefield(player2, "Sheltered Thicket");
        harness.assertInGraveyard(player2, "Sheltered Thicket");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
