package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaptainsHookTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0, menace, and Pirate")
    void equippedCreatureGetsGrants() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hook = addHook(player1);
        hook.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).contains(CardSubtype.PIRATE);
    }

    @Test
    @DisplayName("Re-equipping destroys the previously equipped permanent")
    void reEquipDestroysPreviousPermanent() {
        Permanent hook = addHook(player1);
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        hook.setAttachedTo(firstCreature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        resolveAllTriggers();

        assertThat(hook.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(firstCreature.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equipping from unattached state does not destroy the equipped creature")
    void equippingFromUnattachedDoesNotDestroyCreature() {
        Permanent hook = addHook(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hook.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    private Permanent addHook(Player player) {
        Permanent permanent = new Permanent(new CaptainsHook());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
