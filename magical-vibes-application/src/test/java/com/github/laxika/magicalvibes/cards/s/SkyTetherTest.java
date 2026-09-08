package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkyTetherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sky Tether attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SkyTether()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has defender and loses flying")
    void enchantedCreatureHasDefenderAndLosesFlying() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent tether = new Permanent(new SkyTether());
        tether.setAttachedTo(flyer.getId());
        gd.playerBattlefields.get(player1.getId()).add(tether);

        assertThat(gqs.hasKeyword(gd, flyer, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, flyer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sky Tether does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent tether = new Permanent(new SkyTether());
        tether.setAttachedTo(flyer.getId());
        gd.playerBattlefields.get(player1.getId()).add(tether);

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creature regains flying and loses defender when Sky Tether leaves")
    void creatureRestoresKeywordsWhenAuraLeaves() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent tether = new Permanent(new SkyTether());
        tether.setAttachedTo(flyer.getId());
        gd.playerBattlefields.get(player1.getId()).add(tether);

        gd.playerBattlefields.get(player1.getId()).remove(tether);

        assertThat(gqs.hasKeyword(gd, flyer, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, flyer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SkyTether()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
