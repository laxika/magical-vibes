package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.m.MishrasResearchDesk;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TowerWorker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingCommando.class, GrizzlyBears.class, Shock.class, Disfigure.class,
        MishrasResearchDesk.class, TowerWorker.class})
class WingCommandoTest extends BaseCardTest {

    private Permanent addCommando() {
        Permanent commando = harness.addToBattlefieldAndReturn(player1, new WingCommando());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return commando;
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent commando = addCommando();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent commando = addCommando();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(2);
    }

    @Test
    void noncreatureArtifactTriggersAndMultipleCastsAccumulate() {
        Permanent commando = addCommando();
        harness.setHand(player1, List.of(new MishrasResearchDesk(), new MishrasResearchDesk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(3);
        harness.passBothPriorities();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(4);
    }

    @Test
    void artifactCreatureDoesNotTriggerProwess() {
        Permanent commando = addCommando();
        harness.setHand(player1, List.of(new TowerWorker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(2);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        Permanent commando = addCommando();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(2);
        harness.assertLife(player1, 18);
    }

    @Test
    void prowessResolvesBeforeTheTriggeringSpell() {
        Permanent commando = addCommando();
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, commando.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(3);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wing Commando");
        assertThat(gqs.getEffectivePower(gd, commando)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, commando)).isEqualTo(1);
    }
}
