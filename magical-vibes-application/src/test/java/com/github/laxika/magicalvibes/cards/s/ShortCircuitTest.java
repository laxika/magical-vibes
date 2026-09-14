package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShortCircuit.class, AirElemental.class, FountainOfYouth.class, Plains.class})
class ShortCircuitTest extends BaseCardTest {

    @Test
    @DisplayName("Short Circuit can enchant an artifact")
    void canEnchantArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new ShortCircuit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(aura -> aura.getCard().getName().equals("Short Circuit")
                        && aura.isAttached()
                        && aura.getAttachedTo().equals(artifact.getId()));
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    @DisplayName("Short Circuit gives an enchanted creature -3/-0 and removes flying")
    void debuffsEnchantedCreatureAndRemovesFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent aura = new Permanent(new ShortCircuit());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Short Circuit has no creature effect on an enchanted noncreature artifact")
    void doesNotAffectNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent aura = new Permanent(new ShortCircuit());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(0);
    }

    @Test
    @DisplayName("Short Circuit's effects stop when it leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent aura = new Permanent(new ShortCircuit());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Short Circuit cannot enchant a land")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new ShortCircuit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }
}
