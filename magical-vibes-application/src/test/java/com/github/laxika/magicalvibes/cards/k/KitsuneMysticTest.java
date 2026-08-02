package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KitsuneMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Flips at the end step when enchanted by two Auras")
    void flipsWithTwoAuras() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);
        addAuraAttachedTo(player1, mystic);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isTrue();
        assertThat(mystic.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Does not flip at the end step with fewer than two Auras")
    void doesNotFlipWithOneAura() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not flip if the Aura condition is lost before resolution")
    void doesNotFlipIfConditionIsLostBeforeResolution() {
        Permanent mystic = addMystic();
        addAuraAttachedTo(player1, mystic);
        Permanent secondAura = addAuraAttachedTo(player1, mystic);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(secondAura);
        harness.passBothPriorities();

        assertThat(mystic.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Autumn-Tail moves an Aura to another creature")
    void autumnTailMovesAura() {
        Permanent mystic = addMystic();
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAuraAttachedTo(player1, firstCreature);
        mystic.setTransformed(true);
        mystic.setCard(mystic.getOriginalCard().getBackFaceCard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(aura.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    @DisplayName("Autumn-Tail requires an Aura attached to a creature as its first target")
    void autumnTailRequiresAttachedAuraTarget() {
        Permanent mystic = addMystic();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent unattachedAura = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player1.getId()).add(unattachedAura);
        mystic.setTransformed(true);
        mystic.setCard(mystic.getOriginalCard().getBackFaceCard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(unattachedAura.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMystic() {
        Permanent mystic = new Permanent(new KitsuneMystic());
        mystic.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mystic);
        return mystic;
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return aura;
    }
}
