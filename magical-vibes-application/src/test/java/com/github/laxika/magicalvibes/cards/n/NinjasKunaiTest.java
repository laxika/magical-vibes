package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NinjasKunai.class, GrizzlyBears.class})
class NinjasKunaiTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Ninja's Kunai attaches it to a creature")
    void equipsToCreature() {
        Permanent kunai = addKunaiReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kunai.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature sacrifices Ninja's Kunai to deal 3 damage to a player")
    void sacrificesKunaiToDealDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent kunai = addKunaiReady(player1);
        kunai.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player1, "Ninja's Kunai");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Ninja's Kunai deals 3 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent kunai = addKunaiReady(player1);
        kunai.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Ninja's Kunai");
    }

    private Permanent addKunaiReady(Player player) {
        Permanent perm = new Permanent(new NinjasKunai());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
