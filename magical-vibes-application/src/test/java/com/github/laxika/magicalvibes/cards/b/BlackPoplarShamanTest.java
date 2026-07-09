package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlackPoplarShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration targets a Treefolk and puts ability on stack")
    void activatingTargetsTreefolk() {
        Permanent shaman = addReadyCreature(player1, new BlackPoplarShaman());
        Permanent treefolk = addReadyCreature(player1, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, treefolk.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(treefolk.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield to target Treefolk")
    void resolvingGrantsShield() {
        addReadyCreature(player1, new BlackPoplarShaman());
        Permanent treefolk = addReadyCreature(player1, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate itself (it is a Treefolk)")
    void canRegenerateItself() {
        Permanent shaman = addReadyCreature(player1, new BlackPoplarShaman());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, shaman.getId());
        harness.passBothPriorities();

        assertThat(shaman.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Treefolk creature")
    void cannotTargetNonTreefolk() {
        addReadyCreature(player1, new BlackPoplarShaman());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Treefolk");
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateWithoutMana() {
        Permanent shaman = addReadyCreature(player1, new BlackPoplarShaman());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shaman.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
