package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShieldOfTheAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 of 3 damage when the equipped creature is the only creature you control")
    void preventsOnePerCreatureControlled() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 3 - 1 prevented = 2 damage, lethal to a 2/2
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevention scales with the number of creatures you control")
    void preventionScalesWithCreatureCount() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // 3 creatures controlled → all 3 damage prevented
        assertThat(creature.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Creatures the opponent controls do not increase the prevention")
    void opponentCreaturesDoNotCount() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addShieldReady(player1);
        shield.setAttachedTo(creature.getId());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        // Still only 1 prevented → 2 damage kills the 2/2
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent damage while unattached")
    void doesNotPreventWhileUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addShieldReady(player1);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equip {2} attaches the Shield to target creature you control")
    void equipAttaches() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addShieldReady(Player player) {
        Permanent perm = new Permanent(new ShieldOfTheAvatar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
