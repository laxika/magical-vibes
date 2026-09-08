package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaiiWakeenPerfectShot.class, GrizzlyBears.class, Shock.class, ZuranSpellcaster.class})
class TaiiWakeenPerfectShotTest extends BaseCardTest {

    @Test
    void drawsWhenControlledSourceDealsNoncombatDamageEqualToToughness() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card draw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(draw));

        castShockAt(target);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    void doesNotDrawWhenNoncombatDamageIsNotEqualToToughness() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        Permanent source = addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        prepareMainPhase();
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void addsXToControlledNoncombatDamageForTheTurn() {
        addCreatureReady(player1, new TaiiWakeenPerfectShot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase();
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void castShockAt(Permanent target) {
        prepareMainPhase();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
