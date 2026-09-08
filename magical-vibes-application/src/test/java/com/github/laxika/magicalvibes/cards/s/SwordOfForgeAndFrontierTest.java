package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SwordOfForgeAndFrontierTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from red and green")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage exiles the top two cards for play and grants an extra land play")
    void combatDamageExilesTopTwoAndGrantsExtraLandPlay() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        Card top = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second));
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(top.getId(), second.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
                .contains(top.getId(), second.getId());
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("An unattached Sword does not trigger from combat damage")
    void unattachedSwordDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSwordReady(player1);
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip attaches the Sword to a creature")
    void equipsToCreature() {
        Permanent sword = addSwordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addSwordReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent sword = new Permanent(new SwordOfForgeAndFrontier());
        sword.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sword);
        return sword;
    }
}
