package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordOfTheNazgL.class, GrizzlyBears.class, Shock.class})
class LordOfTheNazgLTest extends BaseCardTest {

    @Test
    void instantCreatesMenaceWraithToken() {
        addLord();

        castShockAndResolveTrigger();

        Permanent wraith = findPermanent(player1, "Wraith");
        assertThat(gqs.getEffectivePower(gd, wraith)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wraith)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, wraith)).contains(CardSubtype.WRAITH);
        assertThat(gqs.hasKeyword(gd, wraith, Keyword.MENACE)).isTrue();
    }

    @Test
    void nineWraithsBecomeNineNineUntilEndOfTurn() {
        Permanent lord = addLord();
        for (int i = 0; i < 7; i++) {
            castShockAndResolveTrigger();
        }

        assertThat(findPermanents(player1, "Wraith")).hasSize(7);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Wraith"))).isEqualTo(3);

        castShockAndResolveTrigger();

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(9);
        for (Permanent wraith : findPermanents(player1, "Wraith")) {
            assertThat(gqs.getEffectivePower(gd, wraith)).isEqualTo(9);
            assertThat(gqs.getEffectiveToughness(gd, wraith)).isEqualTo(9);
        }

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Wraith"))).isEqualTo(3);
    }

    @Test
    void WraithsHaveProtectionFromTheCurrentRingBearer() {
        Permanent lord = addLord();
        castShockAndResolveTrigger();
        Permanent wraith = findPermanent(player1, "Wraith");
        Permanent ringBearer = addCreatureReady(player2, new GrizzlyBears());

        gd.ringLevels.put(player2.getId(), 1);
        gd.ringBearerIds.put(player2.getId(), ringBearer.getId());

        assertThat(gqs.hasProtectionFromSource(gd, wraith, ringBearer)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, lord, ringBearer)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, wraith,
                addCreatureReady(player2, new GrizzlyBears()))).isFalse();
    }

    private Permanent addLord() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new LordOfTheNazgL());
    }

    private void castShockAndResolveTrigger() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
