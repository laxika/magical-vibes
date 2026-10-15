package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GruulWarChant.class, KraulWarrior.class, Opalescence.class})
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
        Permanent bears = addCreatureReady(player1, new KraulWarrior());

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
        Permanent creature = addCreatureReady(controller, new KraulWarrior());
        creature.setAttacking(true);
        return creature;
    }

    @Test
    void effectsEndWhenCreatureStopsAttacking() {
        harness.addToBattlefield(player1, new GruulWarChant());
        Permanent creature = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        creature.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    void multipleWarChantsStackPowerBonuses() {
        harness.addToBattlefield(player1, new GruulWarChant());
        harness.addToBattlefield(player1, new GruulWarChant());
        Permanent creature = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void grantedMenacePreventsSingleBlocker() {
        harness.addToBattlefield(player1, new GruulWarChant());
        addCreatureReady(player1, new KraulWarrior());
        addCreatureReady(player2, new KraulWarrior());

        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void grantedMenaceAllowsTwoBlockers() {
        harness.addToBattlefield(player1, new GruulWarChant());
        addCreatureReady(player1, new KraulWarrior());
        Permanent first = addCreatureReady(player2, new KraulWarrior());
        Permanent second = addCreatureReady(player2, new KraulWarrior());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @CardUsed({GruulWarChant.class, Opalescence.class})
    void animatedWarChantBoostsItselfWhileAttacking() {
        Permanent chant = harness.addToBattlefieldAndReturn(player1, new GruulWarChant());
        harness.addToBattlefield(player1, new Opalescence());
        chant.setAttacking(true);

        assertThat(gqs.isCreature(gd, chant)).isTrue();
        assertThat(gqs.getEffectivePower(gd, chant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, chant)).isEqualTo(4);
    }

    @Test
    @CardUsed({GruulWarChant.class, Opalescence.class})
    void animatedWarChantGrantsItselfMenaceWhileAttacking() {
        Permanent chant = harness.addToBattlefieldAndReturn(player1, new GruulWarChant());
        harness.addToBattlefield(player1, new Opalescence());
        chant.setAttacking(true);

        assertThat(gqs.isCreature(gd, chant)).isTrue();
        assertThat(gqs.hasKeyword(gd, chant, Keyword.MENACE)).isTrue();
    }
}
