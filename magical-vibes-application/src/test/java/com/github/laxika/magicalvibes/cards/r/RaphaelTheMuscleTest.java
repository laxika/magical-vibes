package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphaelTheMuscle.class, GrizzlyBears.class, ZuranSpellcaster.class})
class RaphaelTheMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Mutagen token when it enters")
    void createsMutagenToken() {
        harness.setHand(player1, List.of(new RaphaelTheMuscle()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Doubles damage from a controlled creature with a counter")
    void doublesDamageFromCreatureWithCounter() {
        addCreatureReady(player1, new RaphaelTheMuscle());
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        spellcaster.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(player1, spellcaster), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage from a controlled creature without counters")
    void doesNotDoubleDamageFromCreatureWithoutCounters() {
        addCreatureReady(player1, new RaphaelTheMuscle());
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(player1, spellcaster), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Doubles combat damage from a creature with a counter")
    void doublesCombatDamageFromCreatureWithCounter() {
        addCreatureReady(player1, new RaphaelTheMuscle());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(battlefieldIndex(player1, attacker)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
