package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeconstructionHammer.class, GrizzlyBears.class, GloriousAnthem.class, LeoninScimitar.class})
class DeconstructionHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature can sacrifice Deconstruction Hammer to destroy an artifact")
    void destroysTargetArtifact() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deconstruction Hammer");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Equipped creature can destroy an enchantment")
    void destroysTargetEnchantment() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Granted ability cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addReadyCreature(player1);
        Permanent hammer = addReadyHammer(player1);
        hammer.setAttachedTo(creature.getId());
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyHammer(Player player) {
        Permanent perm = new Permanent(new DeconstructionHammer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyEnchantment(Player player) {
        Permanent perm = new Permanent(new GloriousAnthem());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
