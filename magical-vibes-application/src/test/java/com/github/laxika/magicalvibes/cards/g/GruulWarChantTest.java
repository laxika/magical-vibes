package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GruulWarChantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0 and have menace")
    void buffsAndGrantsMenaceToOwnAttackers() {
        harness.addToBattlefield(player1, new GruulWarChant());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures you control are unaffected")
    void doesNotAffectNonAttackers() {
        harness.addToBattlefield(player1, new GruulWarChant());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's attacking creatures are unaffected")
    void doesNotAffectOpponentAttackers() {
        harness.addToBattlefield(player1, new GruulWarChant());
        Permanent bears = addAttackingBears(player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Effects end when Gruul War Chant leaves the battlefield")
    void effectsRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new GruulWarChant());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Gruul War Chant"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        gd.playerBattlefields.get(controller.getId()).add(creature);
        return creature;
    }
}
