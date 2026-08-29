package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiplomaticImmunityTest extends BaseCardTest {

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addReadyCreature();
        harness.setHand(player1, List.of(new DiplomaticImmunity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    void grantsShroudToEnchantedCreatureAndAura() {
        Permanent creature = addReadyCreature();
        Permanent aura = attachAura(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isTrue();
    }

    @Test
    void shroudPreventsTargetingEnchantedCreatureAndAura() {
        Permanent creature = addReadyCreature();
        Permanent aura = attachAura(creature);

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, creature.getId(), null))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, aura.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shroudEndsWhenAuraLeavesBattlefield() {
        Permanent creature = addReadyCreature();
        Permanent aura = attachAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DiplomaticImmunity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyCreature() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new DiplomaticImmunity());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
