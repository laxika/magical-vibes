package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManiacalRage.class, RagingGoblin.class, Spellbook.class})
class ManiacalRageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Maniacal Rage attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        ManiacalRage rage = new ManiacalRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == rage
                        && p.isAttached()
                        && goblin.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ManiacalRage());
        aura.setAttachedTo(goblin.getId());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature can't block")
    void enchantedCreatureCantBlock() {
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());

        assertThat(bls.canBlock(gd, goblin)).isTrue();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ManiacalRage());
        aura.setAttachedTo(goblin.getId());

        assertThat(bls.canBlock(gd, goblin)).isFalse();
    }

    @Test
    @DisplayName("Creature returns to base stats and can block again when Maniacal Rage is removed")
    void effectsStopWhenRemoved() {
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ManiacalRage());
        aura.setAttachedTo(goblin.getId());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(bls.canBlock(gd, goblin)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
        assertThat(bls.canBlock(gd, goblin)).isTrue();
    }

    @Test
    @DisplayName("Maniacal Rage can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        ManiacalRage rage = new ManiacalRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == rage
                        && p.isAttached()
                        && goblin.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(3);
    }

    @Test
    @DisplayName("Maniacal Rage goes to its owner's graveyard when its target is removed")
    void fizzlesIfTargetRemoved() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        ManiacalRage rage = new ManiacalRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, goblin.getId());
        gd.playerBattlefields.get(player1.getId()).remove(goblin);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rage);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == rage);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Maniacal Rage")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RagingGoblin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new ManiacalRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
