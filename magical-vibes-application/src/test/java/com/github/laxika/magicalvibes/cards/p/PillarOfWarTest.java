package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PillarOfWarTest extends BaseCardTest {

    private Permanent addPillarReady() {
        return addCreatureReady(player1, new PillarOfWar());
    }

    private Permanent attachHolyStrength(Permanent creature) {
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Cannot attack while unenchanted")
    void cannotAttackWhileUnenchanted() {
        addPillarReady();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack while enchanted")
    void canAttackWhileEnchanted() {
        Permanent pillar = addPillarReady();
        attachHolyStrength(pillar);
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(pillar.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot attack after the Aura leaves")
    void cannotAttackAfterAuraLeaves() {
        Permanent pillar = addPillarReady();
        Permanent aura = attachHolyStrength(pillar);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
