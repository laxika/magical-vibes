package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoldveinPickTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Goldvein Pick to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent pick = addPickReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pick.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Goldvein Pick does not affect an unequipped creature")
    void doesNotAffectUnequippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addPickReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Treasure token when equipped creature deals combat damage to a player")
    void createsTreasureTokenOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(treasuresFor(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure token when equipped creature deals no combat damage to a player")
    void doesNotCreateTreasureWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(treasuresFor(player1)).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        addPickReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPickReady(Player player) {
        Permanent perm = new Permanent(new GoldveinPick());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Permanent> treasuresFor(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
    }
}
