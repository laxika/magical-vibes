package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoninWarclub.class, GnarledMass.class})
class RoninWarclubTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        Permanent warclub = addWarclubReady(player1);
        warclub.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolving equip attaches Ronin Warclub to target creature")
    void resolvingEquipAttaches() {
        Permanent warclub = addWarclubReady(player1);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(warclub.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Whenever a creature you control enters, Ronin Warclub attaches to it")
    void attachesToEnteringCreature() {
        Permanent warclub = addWarclubReady(player1);

        harness.setHand(player1, List.of(new GnarledMass()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent creature = findPermanent(player1, "Gnarled Mass");
        assertThat(warclub.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature an opponent controls entering does not trigger Ronin Warclub")
    void opponentCreatureDoesNotTriggerAttachment() {
        Permanent warclub = addWarclubReady(player1);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GnarledMass()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(warclub.getAttachedTo()).isNull();
    }

    private Permanent addWarclubReady(Player player) {
        Permanent perm = new Permanent(new RoninWarclub());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
