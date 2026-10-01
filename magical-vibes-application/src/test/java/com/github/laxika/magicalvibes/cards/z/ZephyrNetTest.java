package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CennsHeir;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.cards.t.TurtleshellChangeling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZephyrNet.class, CennsHeir.class, TurtleshellChangeling.class, SpringleafDrum.class})
class ZephyrNetTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Zephyr Net attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent target = addCreatureReady(player1, new CennsHeir());
        ZephyrNet net = new ZephyrNet();

        harness.setHand(player1, List.of(net));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == net
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has defender and flying")
    void enchantedCreatureHasDefenderAndFlying() {
        Permanent target = addCreatureReady(player1, new CennsHeir());

        Permanent netPerm = harness.addToBattlefieldAndReturn(player1, new ZephyrNet());
        netPerm.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted opponent's creature has defender and flying")
    void enchantedOpponentsCreatureHasDefenderAndFlying() {
        Permanent target = addCreatureReady(player2, new TurtleshellChangeling());

        Permanent netPerm = harness.addToBattlefieldAndReturn(player1, new ZephyrNet());
        netPerm.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Zephyr Net does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent target = addCreatureReady(player1, new CennsHeir());

        Permanent otherCreature = addCreatureReady(player1, new TurtleshellChangeling());

        Permanent netPerm = harness.addToBattlefieldAndReturn(player1, new ZephyrNet());
        netPerm.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creature loses defender and flying when Zephyr Net leaves")
    void creatureLosesKeywordsWhenAuraLeaves() {
        Permanent target = addCreatureReady(player1, new CennsHeir());

        Permanent netPerm = harness.addToBattlefieldAndReturn(player1, new ZephyrNet());
        netPerm.setAttachedTo(target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(netPerm);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantANoncreaturePermanent() {
        addCreatureReady(player2, new CennsHeir());
        Permanent drum = harness.addToBattlefieldAndReturn(player1, new SpringleafDrum());
        harness.setHand(player1, List.of(new ZephyrNet()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, drum.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
