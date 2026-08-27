package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinstrikersWill.class, GrizzlyBears.class, Plains.class})
class SinstrikersWillTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sinstriker's Will attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SinstrikersWill()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SinstrikersWill
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature deals damage equal to its power to an attacking creature")
    void dealsPowerDamageToAttackingCreature() {
        Permanent source = addAttachedAura(new Permanent(new GrizzlyBears()));
        source.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can target a blocking creature")
    void dealsPowerDamageToBlockingCreature() {
        Permanent source = addAttachedAura(new Permanent(new GrizzlyBears()));
        source.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setBlocking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("The granted ability cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent source = addAttachedAura(new Permanent(new GrizzlyBears()));
        source.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    private Permanent addAttachedAura(Permanent enchantedCreature) {
        gd.playerBattlefields.get(player1.getId()).add(enchantedCreature);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SinstrikersWill());
        aura.setAttachedTo(enchantedCreature.getId());
        return enchantedCreature;
    }
}
