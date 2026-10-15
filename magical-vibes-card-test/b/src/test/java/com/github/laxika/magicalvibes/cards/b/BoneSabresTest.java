package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SicarianInfiltrator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneSabres.class, SicarianInfiltrator.class})
class BoneSabresTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsFourCountersWhenAttacking() {
        Permanent creature = addCreatureReady(player1, new SicarianInfiltrator());
        Permanent sabres = addCreatureReady(player1, new BoneSabres());
        sabres.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void unattachedSabresDoesNotPutCountersOnAttackingCreature() {
        Permanent creature = addCreatureReady(player1, new SicarianInfiltrator());
        addCreatureReady(player1, new BoneSabres());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void equipAttachesBoneSabresToCreatureYouControl() {
        Permanent sabres = addCreatureReady(player1, new BoneSabres());
        Permanent creature = addCreatureReady(player1, new SicarianInfiltrator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sabres.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void countersStayWithAttackerWhenEquipmentMovesBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new SicarianInfiltrator());
        Permanent other = addCreatureReady(player1, new SicarianInfiltrator());
        Permanent sabres = addCreatureReady(player1, new BoneSabres());
        sabres.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        sabres.setAttachedTo(other.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackerGetsCountersEvenIfSabresBecomesUnattached() {
        Permanent attacker = addCreatureReady(player1, new SicarianInfiltrator());
        Permanent sabres = addCreatureReady(player1, new BoneSabres());
        sabres.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        sabres.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void attackerGetsCountersEvenIfSabresLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new SicarianInfiltrator());
        Permanent sabres = addCreatureReady(player1, new BoneSabres());
        sabres.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(sabres);
        gd.playerGraveyards.get(player1.getId()).add(sabres.getCard());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        addCreatureReady(player1, new BoneSabres());
        Permanent opponent = addCreatureReady(player2, new SicarianInfiltrator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotActivateDuringCombat() {
        addCreatureReady(player1, new BoneSabres());
        Permanent creature = addCreatureReady(player1, new SicarianInfiltrator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }
}
