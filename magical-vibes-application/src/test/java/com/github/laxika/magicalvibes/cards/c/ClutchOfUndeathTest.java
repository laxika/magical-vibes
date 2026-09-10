package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshnodsCylix;
import com.github.laxika.magicalvibes.cards.b.BenthicExplorers;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClutchOfUndeath.class, ZombieGoliath.class, BenthicExplorers.class, AshnodsCylix.class})
class ClutchOfUndeathTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent clutch = harness.addToBattlefieldAndReturn(player1, new ClutchOfUndeath());
        clutch.setAttachedTo(creature.getId());
        return clutch;
    }

    @Test
    @DisplayName("Zombie enchanted creature gets +3/+3")
    void zombieCreatureGetsBoost() {
        Permanent zombie = addCreatureReady(player1, new ZombieGoliath());
        int basePower = gqs.getEffectivePower(gd, zombie);
        int baseToughness = gqs.getEffectiveToughness(gd, zombie);

        attach(zombie);

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Non-Zombie enchanted creature gets -3/-3 instead")
    void nonZombieCreatureGetsPenalty() {
        Permanent nonZombie = addCreatureReady(player1, new BenthicExplorers());
        int basePower = gqs.getEffectivePower(gd, nonZombie);
        int baseToughness = gqs.getEffectiveToughness(gd, nonZombie);

        attach(nonZombie);

        assertThat(gqs.getEffectivePower(gd, nonZombie)).isEqualTo(basePower - 3);
        assertThat(gqs.getEffectiveToughness(gd, nonZombie)).isEqualTo(baseToughness - 3);
    }

    @Test
    @DisplayName("Modification wears off when Clutch of Undeath leaves the battlefield")
    void modificationRemovedWhenAuraLeaves() {
        Permanent zombie = addCreatureReady(player1, new ZombieGoliath());
        int basePower = gqs.getEffectivePower(gd, zombie);

        Permanent clutch = attach(zombie);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(basePower + 3);

        gd.playerBattlefields.get(player1.getId()).remove(clutch);

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Resolving Clutch of Undeath attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new BenthicExplorers());

        harness.setHand(player1, List.of(new ClutchOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ClutchOfUndeath
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AshnodsCylix());
        harness.setHand(player1, List.of(new ClutchOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
