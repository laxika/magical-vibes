package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.f.FiligreeFamiliar;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.w.WilyBandar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VedalkenBlademaster.class, ChandrasPyrohelix.class, WilyBandar.class,
        PropheticPrism.class, FiligreeFamiliar.class})
class VedalkenBlademasterTest extends BaseCardTest {

    private Permanent addBlademaster() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new VedalkenBlademaster());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return blademaster;
    }

    @Test
    @DisplayName("Prowess gives Vedalken Blademaster +1/+1 when its controller casts a noncreature spell")
    void noncreatureSpellPumps() {
        Permanent blademaster = addBlademaster();
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(4);
    }

    @Test
    @DisplayName("Prowess does not trigger when its controller casts a creature spell")
    void creatureSpellDoesNotPump() {
        Permanent blademaster = addBlademaster();
        harness.setHand(player1, List.of(new WilyBandar()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blademaster = addBlademaster();
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(3);
    }

    @Test
    void prowessResolvesBeforeTheSpellAndStacksForEachCast() {
        Permanent blademaster = addBlademaster();
        harness.setHand(player1, List.of(new ChandrasPyrohelix(), new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 4);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        Permanent blademaster = addBlademaster();
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, Map.of(player1.getId(), 2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(3);
    }

    @Test
    void prowessTriggersDuringOpponentsTurn() {
        Permanent blademaster = addBlademaster();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(4);
    }

    @Test
    void noncreatureArtifactSpellTriggersProwess() {
        Permanent blademaster = addBlademaster();
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
    }

    @Test
    void artifactCreatureSpellDoesNotTriggerProwess() {
        Permanent blademaster = addBlademaster();
        harness.setHand(player1, List.of(new FiligreeFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blademaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blademaster)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Filigree Familiar");
    }
}
