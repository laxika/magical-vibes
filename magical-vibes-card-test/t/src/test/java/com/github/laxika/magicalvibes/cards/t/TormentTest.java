package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HornOfGreed;
import com.github.laxika.magicalvibes.cards.v.VenerableMonk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Torment.class, HornOfGreed.class, VenerableMonk.class})
class TormentTest extends BaseCardTest {

    @Test
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new Torment()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new Torment()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void enchantedCreatureGetsDebuff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        Permanent torment = new Permanent(new Torment());
        torment.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(torment);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void canEnchantOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new VenerableMonk());
        harness.setHand(player1, List.of(new Torment()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        Permanent torment = new Permanent(new Torment());
        torment.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(torment);

        gd.playerBattlefields.get(player1.getId()).remove(torment);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void fizzlesIfTargetCreatureIsRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new VenerableMonk());
        harness.setHand(player1, List.of(new Torment()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Torment");
        harness.assertNotOnBattlefield(player1, "Torment");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HornOfGreed());
        harness.setHand(player1, List.of(new Torment()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleCopiesStackWithoutReducingToughnessOrAffectingOtherCreatures() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new VenerableMonk());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new VenerableMonk());
        harness.setHand(player1, List.of(new Torment(), new Torment()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void auraGoesToItsOwnersGraveyardWhenEnchantedCreatureLeaves() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new VenerableMonk());
        harness.setHand(player1, List.of(new Torment()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(enchanted);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Torment");
        harness.assertNotInGraveyard(player2, "Torment");
        harness.assertNotOnBattlefield(player1, "Torment");
    }
}
