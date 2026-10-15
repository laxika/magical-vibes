package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CastIntoDarkness.class, GrizzlyBears.class, PensiveMinotaur.class})
class CastIntoDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cast into Darkness attaches it and gives the creature -2/-0")
    void castAttachesAndReducesPower() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CastIntoDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cast into Darkness")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new CastIntoDarkness());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature can still attack")
    void enchantedCreatureCanAttack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new CastIntoDarkness());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature without affecting other creatures")
    void canEnchantOpponentsCreature() {
        Permanent enchanted = addCreatureReady(player2, new PensiveMinotaur());
        Permanent other = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new CastIntoDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0, enchanted.getId());

        assertThat(findPermanent(player1, "Cast into Darkness").getAttachedTo())
                .isEqualTo(enchanted.getId());
        assertThat(gqs.getEffectivePower(gd, enchanted)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple copies stack and can reduce power below zero without reducing toughness")
    void multipleCopiesStack() {
        Permanent creature = addCreatureReady(player2, new PensiveMinotaur());
        harness.setHand(player1, List.of(new CastIntoDarkness(), new CastIntoDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());
        harness.castAndResolveSorcery(player1, 0, 0, creature.getId());

        assertThat(countPermanents(player1, "Cast into Darkness")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }
}
