package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuardDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Guard Duty attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GuardDuty
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has defender")
    void enchantedCreatureHasDefender() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachGuardDuty(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Guard Duty does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        attachGuardDuty(enchanted);

        assertThat(gqs.hasKeyword(gd, other, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Creature loses defender when Guard Duty leaves the battlefield")
    void creatureLosesDefenderWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachGuardDuty(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Guard Duty cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachGuardDuty(Permanent creature) {
        Permanent aura = new Permanent(new GuardDuty());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
