package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CosmiumBlast.class, ColossalDreadmaw.class, GrizzlyBears.class})
class CosmiumBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to an attacking creature")
    void dealsDamageToAttackingCreature() {
        Permanent attacker = addCombatCreature(player2, new ColossalDreadmaw(), "Colossal Dreadmaw", true);

        castSpellAt(attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Deals 4 damage to a blocking creature")
    void dealsDamageToBlockingCreature() {
        Permanent blocker = addCombatCreature(player2, new ColossalDreadmaw(), "Colossal Dreadmaw", false);

        castSpellAt(blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CosmiumBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private void castSpellAt(UUID targetId) {
        harness.setHand(player1, List.of(new CosmiumBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addCombatCreature(Player owner, Card card, String name, boolean attacking) {
        harness.addToBattlefield(owner, card);
        Permanent permanent = findPermanent(owner, name);
        permanent.setSummoningSick(false);
        if (attacking) {
            permanent.setAttacking(true);
            permanent.setAttackTarget(player1.getId());
        } else {
            permanent.setBlocking(true);
            permanent.addBlockingTargetId(UUID.randomUUID());
        }
        return permanent;
    }
}
