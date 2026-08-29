package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DredgingClawTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and menace")
    void equippedCreatureGetsBoostAndMenace() {
        Permanent creature = addCreatureReady(player1, new ReassemblingSkeleton());
        Permanent claw = addClawReady(player1);
        claw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Resolving equip attaches Dredging Claw to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent claw = addClawReady(player1);
        Permanent creature = addCreatureReady(player1, new ReassemblingSkeleton());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(claw.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("May attach to a creature entering from your graveyard")
    void mayAttachToCreatureEnteringFromGraveyard() {
        Permanent claw = addClawReady(player1);
        harness.setGraveyard(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        for (int i = 0; i < 4 && !gd.interaction.isAwaitingInput() && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent skeleton = findPermanent(player1, "Reassembling Skeleton");
        assertThat(claw.getAttachedTo()).isEqualTo(skeleton.getId());
    }

    private Permanent addClawReady(Player player) {
        Permanent perm = new Permanent(new DredgingClaw());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
