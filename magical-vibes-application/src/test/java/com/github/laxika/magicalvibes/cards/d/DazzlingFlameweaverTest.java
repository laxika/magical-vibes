package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BladeJuggler;
import com.github.laxika.magicalvibes.cards.b.BodyCount;
import com.github.laxika.magicalvibes.cards.h.Hackrobat;
import com.github.laxika.magicalvibes.cards.l.LightUpTheStage;
import com.github.laxika.magicalvibes.cards.r.RafterDemon;
import com.github.laxika.magicalvibes.cards.r.RixMaadiReveler;
import com.github.laxika.magicalvibes.cards.s.SkewerTheCritics;
import com.github.laxika.magicalvibes.cards.s.SpawnOfMayhem;
import com.github.laxika.magicalvibes.cards.s.SpikewheelAcrobat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DazzlingFlameweaver.class, GrizzlyBears.class, BladeJuggler.class, BodyCount.class,
        DeadRevels.class, DrillBit.class, Hackrobat.class, LightUpTheStage.class,
        RafterDemon.class, RixMaadiReveler.class, SkewerTheCritics.class, SpawnOfMayhem.class,
        SpikewheelAcrobat.class})
class DazzlingFlameweaverTest extends BaseCardTest {

    @Test
    void combatDamageConjuresOneRandomSpellbookCardIntoExileWithNextTurnPermission() {
        harness.addToBattlefield(player1, new DazzlingFlameweaver());
        addAttacker(new GrizzlyBears());

        runCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        var exiled = gd.getPlayerExiledCards(player1.getId()).getFirst();
        assertThat(exiled.getName()).isIn(Set.of(
                "Blade Juggler", "Body Count", "Dead Revels", "Drill Bit", "Hackrobat",
                "Light Up the Stage", "Rafter Demon", "Rix Maadi Reveler", "Skewer the Critics",
                "Spawn of Mayhem", "Spikewheel Acrobat"));
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(exiled.getId());
    }

    @Test
    void twoCreaturesDealingCombatDamageTriggerOnlyOnce() {
        harness.addToBattlefield(player1, new DazzlingFlameweaver());
        addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());

        runCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    private void addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
