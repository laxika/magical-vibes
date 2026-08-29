package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HaywireMiteTest extends BaseCardTest {

    @Test
    @DisplayName("When Haywire Mite dies, its controller gains 2 life")
    void gainsLifeWhenItDies() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Haywire Mite"));
        harness.passBothPriorities();

        resolveAllTriggers();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Sacrificing Haywire Mite exiles a noncreature artifact")
    void exilesNoncreatureArtifact() {
        harness.addToBattlefield(player1, new HaywireMite());
        harness.addToBattlefield(player2, new Millstone());
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = findPermanent(player2, "Millstone");
        harness.activateAbility(player1, 0, null, target.getId());

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

        Permanent target = findPermanent(player2, "Glorious Anthem");
        harness.activateAbility(player1, 0, null, target.getId());
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

        Permanent target = findPermanent(player2, "Iron Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact or noncreature enchantment");
    }
}
