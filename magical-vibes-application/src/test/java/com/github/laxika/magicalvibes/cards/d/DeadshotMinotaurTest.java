package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnigmaSphinx;
import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.cards.w.WingedCoatl;
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

@CardUsed({DeadshotMinotaur.class, EnigmaSphinx.class, GrizzledLeotau.class, WingedCoatl.class})
class DeadshotMinotaurTest extends BaseCardTest {


    @Test
    @DisplayName("ETB deals 3 damage to target creature with flying")
    void etbDeals3DamageToFlyer() {
        harness.addToBattlefield(player2, new EnigmaSphinx());
        UUID targetId = harness.getPermanentId(player2, "Enigma Sphinx");

        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        addManaCost(player1);

        harness.castCreature(player1, 0, targetId);

        // Resolve the creature spell, then its triggered ability.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent flyer = findPermanent(player2, "Enigma Sphinx");
        assertThat(flyer.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB kills a 1/1 flyer")
    void etbKillsSmallFlyer() {
        harness.addToBattlefield(player2, new WingedCoatl());
        UUID targetId = harness.getPermanentId(player2, "Winged Coatl");

        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        addManaCost(player1);

        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Winged Coatl");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefield(player2, new GrizzledLeotau());
        UUID targetId = harness.getPermanentId(player2, "Grizzled Leotau");

        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        addManaCost(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Cycling discards the card and draws one, paid with red")
    void cyclingDrawsACardWithRed() {
        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        harness.setLibrary(player1, List.of(new GrizzledLeotau()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deadshot Minotaur");
        harness.assertInHand(player1, "Grizzled Leotau");
    }

    @Test
    @DisplayName("Cycling can be paid with green")
    void cyclingDrawsACardWithGreen() {
        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        harness.setLibrary(player1, List.of(new GrizzledLeotau()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deadshot Minotaur");
        harness.assertInHand(player1, "Grizzled Leotau");
    }

    @Test
    @DisplayName("Can enter when no creatures have flying")
    void entersWithoutLegalTargets() {
        harness.addToBattlefield(player2, new GrizzledLeotau());
        harness.castFromHand(player1, new DeadshotMinotaur(), "{3}{R}{G}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deadshot Minotaur");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Grizzled Leotau").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("ETB can target a flying creature its controller owns")
    void etbCanTargetOwnFlyer() {
        harness.addToBattlefield(player1, new WingedCoatl());
        UUID targetId = harness.getPermanentId(player1, "Winged Coatl");
        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        addManaCost(player1);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Winged Coatl");
        harness.assertOnBattlefield(player1, "Deadshot Minotaur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still deals damage after its source leaves the battlefield")
    void etbResolvesWithoutSource() {
        harness.addToBattlefield(player2, new EnigmaSphinx());
        UUID targetId = harness.getPermanentId(player2, "Enigma Sphinx");
        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        addManaCost(player1);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(findPermanent(player1, "Deadshot Minotaur").getCard()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Enigma Sphinx").getMarkedDamage()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards immediately but draws only when the ability resolves")
    void cyclingPaysDiscardBeforeDrawing() {
        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        harness.setLibrary(player1, List.of(new GrizzledLeotau()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Deadshot Minotaur");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzled Leotau");
        harness.assertNotOnBattlefield(player1, "Deadshot Minotaur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be paid with colorless mana")
    void cyclingRejectsColorlessMana() {
        harness.setHand(player1, List.of(new DeadshotMinotaur()));
        harness.setLibrary(player1, List.of(new GrizzledLeotau()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Deadshot Minotaur");
        harness.assertNotInGraveyard(player1, "Deadshot Minotaur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB chooses its target after the creature spell resolves")
    void choosesTargetAfterEntering() {
        harness.castFromHand(player1, new DeadshotMinotaur(), "{3}{R}{G}");
        harness.addToBattlefield(player2, new WingedCoatl());
        UUID targetId = harness.getPermanentId(player2, "Winged Coatl");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deadshot Minotaur");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Winged Coatl");
        assertThat(gd.stack).isEmpty();
    }

    private void addManaCost(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 4);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
