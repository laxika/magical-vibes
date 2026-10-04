package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaywireMite.class, GloriousAnthem.class, IronMyr.class, Millstone.class, Shock.class})
class HaywireMiteTest extends BaseCardTest {

    @Test
    @DisplayName("When Haywire Mite dies, its controller gains 2 life")
    void gainsLifeWhenItDies() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Haywire Mite"));
        resolveAllTriggers();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Sacrificing Haywire Mite exiles a noncreature artifact")
    void exilesNoncreatureArtifact() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.addToBattlefield(player2, new Millstone());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Millstone"));

        harness.assertNotOnBattlefield(player1, "Haywire Mite");
        harness.assertInGraveyard(player1, "Haywire Mite");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Millstone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Millstone"));
    }

    @Test
    @DisplayName("Sacrificing Haywire Mite exiles a noncreature enchantment")
    void exilesNoncreatureEnchantment() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Glorious Anthem"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Iron Myr")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact or noncreature enchantment");
    }

    @Test
    @DisplayName("Can exile its controller's artifact and gains life from the sacrifice")
    void exilesOwnArtifactAndGainsLife() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Millstone"));

        harness.assertInGraveyard(player1, "Haywire Mite");
        harness.assertLife(player1, 10);
        harness.assertOnBattlefield(player1, "Millstone");
        harness.passBothPriorities();
        harness.assertLife(player1, 12);
        harness.assertOnBattlefield(player1, "Millstone");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertNotInGraveyard(player1, "Millstone");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Millstone"));
    }

    @Test
    @DisplayName("Cannot activate without green mana and does not sacrifice the Mite")
    void cannotActivateWithoutGreenMana() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.addToBattlefield(player2, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Millstone")))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Haywire Mite");
        harness.assertNotInGraveyard(player1, "Haywire Mite");
        harness.assertOnBattlefield(player2, "Millstone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both death triggers gain life even when the first exile ability loses its target")
    void gainsLifeEvenWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.addToBattlefield(player1, new HaywireMite());
        harness.addToBattlefield(player2, new Millstone());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);
        var targetId = harness.getPermanentId(player2, "Millstone");

        harness.activateAbility(player1, 0, null, targetId);
        harness.activateAbility(player1, 0, null, targetId);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertNotOnBattlefield(player1, "Haywire Mite");
        harness.assertNotOnBattlefield(player2, "Millstone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Millstone"))
                .hasSize(1);
    }
}
