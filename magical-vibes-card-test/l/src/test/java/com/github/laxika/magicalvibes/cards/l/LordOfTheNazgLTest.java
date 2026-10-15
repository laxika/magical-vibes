package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.Earthquake;
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

@CardUsed({LordOfTheNazgL.class, GrizzlyBears.class, Shock.class, Earthquake.class})
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


    @Test
    void sorceryCreatesWraithBeforeSpellResolves() {
        addLord();
        harness.setHand(player1, List.of(new Earthquake()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        assertThat(findPermanents(player1, "Wraith")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Wraith")).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    void creatureSpellDoesNotCreateWraith() {
        addLord();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wraith")).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentInstantDoesNotCreateWraith() {
        addLord();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wraith")).isEmpty();
        assertThat(findPermanents(player2, "Wraith")).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    void triggerStillCreatesTokenAfterLordLeaves() {
        Permanent lord = addLord();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lord);
        gd.playerGraveyards.get(player1.getId()).add(lord.getCard());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wraith")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Wraith"))).isEqualTo(3);
    }

    @Test
    void wraithCountIsCheckedAtResolution() {
        Permanent lord = addLord();
        for (int i = 0; i < 7; i++) {
            castShockAndResolveTrigger();
        }
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Wraith"));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Wraith")).hasSize(7);
        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(4);
        for (Permanent wraith : findPermanents(player1, "Wraith")) {
            assertThat(gqs.getEffectivePower(gd, wraith)).isEqualTo(3);
        }
    }

    @Test
    void nineNineEffectOnlyAffectsOwnWraithsPresentAtResolution() {
        Permanent lord = addLord();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castShockAndResolveTrigger();
        Permanent opponentWraith = harness.addToBattlefieldAndReturn(
                player2, findPermanent(player1, "Wraith").getCard());
        for (int i = 0; i < 7; i++) {
            castShockAndResolveTrigger();
        }
        Permanent laterWraith = harness.addToBattlefieldAndReturn(
                player1, findPermanent(player1, "Wraith").getCard());
        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentWraith)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, laterWraith)).isEqualTo(3);
    }

    @Test
    void protectionTracksRingBearerAndEndsWhenLordLeaves() {
        Permanent lord = addLord();
        castShockAndResolveTrigger();
        Permanent wraith = findPermanent(player1, "Wraith");
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstBearer = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBearer = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentWraith = harness.addToBattlefieldAndReturn(player2, wraith.getCard());
        gd.ringLevels.put(player2.getId(), 1);
        gd.ringBearerIds.put(player2.getId(), firstBearer.getId());
        assertThat(gqs.hasProtectionFromSource(gd, wraith, firstBearer)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, ownBear, firstBearer)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, opponentWraith, firstBearer)).isFalse();
        gd.ringBearerIds.put(player2.getId(), secondBearer.getId());
        assertThat(gqs.hasProtectionFromSource(gd, wraith, firstBearer)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, wraith, secondBearer)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(lord);
        gd.playerGraveyards.get(player1.getId()).add(lord.getCard());
        assertThat(gqs.hasProtectionFromSource(gd, wraith, secondBearer)).isFalse();
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
        resolveAllTriggers();
    }
}
