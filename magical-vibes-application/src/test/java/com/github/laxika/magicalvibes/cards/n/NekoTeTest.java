package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.t.TakenosCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NekoTe.class, TakenosCavalry.class, GnarledMass.class})
class NekoTeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature damaging a creature taps it and keeps it tapped")
    void equippedCreatureDamagingCreatureTapsAndLocksIt() {
        Permanent cavalry = addCreatureReady(player1, new TakenosCavalry());
        Permanent nekoTe = attachNekoTe(player1, cavalry);
        Permanent target = addCreatureReady(player2, new GnarledMass());
        target.setAttacking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();

        advanceToUpkeep(player2);

        assertThat(target.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(nekoTe);
        advanceToUpkeep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equipped creature damaging a player makes that player lose 1 life")
    void equippedCreatureDamagingPlayerCausesLifeLoss() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        attachNekoTe(player1, attacker);
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Resolving equip attaches Neko-Te to a creature")
    void resolvingEquipAttachesToTargetCreature() {
        Permanent nekoTe = new Permanent(new NekoTe());
        gd.playerBattlefields.get(player1.getId()).add(nekoTe);
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(nekoTe.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent attachNekoTe(Player player, Permanent host) {
        Permanent nekoTe = new Permanent(new NekoTe());
        nekoTe.setSummoningSick(false);
        nekoTe.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(nekoTe);
        return nekoTe;
    }
}
