package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LitaMechanicalEngineer.class, GrizzlyBears.class})
class LitaMechanicalEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 flying Zeppelin Vehicle token with crew 3")
    void createsZeppelinToken() {
        Permanent lita = addLitaReady(player1);
        addLitaMana();

        createZeppelin(lita);

        Permanent zeppelin = findZeppelin();
        assertThat(gqs.isArtifact(gd, zeppelin)).isTrue();
        assertThat(gqs.isCreature(gd, zeppelin)).isFalse();
        assertThat(gqs.getEffectivePower(gd, zeppelin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zeppelin)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, zeppelin, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Untaps other artifact creatures at your end step")
    void untapsOtherArtifactCreaturesAtEndStep() {
        Permanent lita = addLitaReady(player1);
        addLitaMana();
        Permanent zeppelin = createZeppelin(lita);
        Permanent bear = addCreatureReady(player1);
        Permanent secondBear = addCreatureReady(player1);

        harness.activateAbility(player1, battlefieldIndex(zeppelin), null, null);
        harness.passBothPriorities();

        zeppelin.tap();
        assertThat(bear.isTapped()).isTrue();
        assertThat(secondBear.isTapped()).isTrue();

        advanceToEndStep();

        assertThat(zeppelin.isTapped()).isFalse();
        assertThat(lita.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        assertThat(secondBear.isTapped()).isTrue();
    }

    @Test
    void doesNotUntapUncrewedVehiclesOrOpponentsArtifactCreatures() {
        Permanent lita = addLitaReady(player1);
        addLitaMana();
        Permanent zeppelin = createZeppelin(lita);
        zeppelin.tap();
        Permanent opposingLita = addLitaReady(player2);
        opposingLita.tap();

        advanceToEndStep();

        assertThat(zeppelin.isTapped()).isTrue();
        assertThat(opposingLita.isTapped()).isTrue();
        assertThat(lita.isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        Permanent lita = addLitaReady(player1);
        addLitaMana();
        Permanent zeppelin = createZeppelin(lita);
        addCreatureReady(player1);
        addCreatureReady(player1);
        harness.activateAbility(player1, battlefieldIndex(zeppelin), null, null);
        harness.passBothPriorities();
        zeppelin.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(zeppelin.isTapped()).isTrue();
    }

    @Test
    void cannotCreateZeppelinWhileSummoningSick() {
        Permanent lita = addLitaReady(player1);
        lita.setSummoningSick(true);
        addLitaMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(lita), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Zeppelin")).isZero();
        assertThat(lita.isTapped()).isFalse();
    }

    @Test
    void crewRequiresAtLeastThreePower() {
        Permanent lita = addLitaReady(player1);
        addLitaMana();
        Permanent zeppelin = createZeppelin(lita);
        Permanent bear = addCreatureReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(zeppelin), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bear.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, zeppelin)).isFalse();
    }

    @Test
    void summoningSickCreaturesCanCrewZeppelin() {
        Permanent lita = addLitaReady(player1);
        addLitaMana();
        Permanent zeppelin = createZeppelin(lita);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setSummoningSick(true);
        secondBear.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(zeppelin), null, null);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(secondBear.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, zeppelin)).isTrue();
        assertThat(gqs.getEffectivePower(gd, zeppelin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zeppelin)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, zeppelin, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, zeppelin)).isFalse();
        assertThat(gqs.isArtifact(gd, zeppelin)).isTrue();
    }

    private Permanent createZeppelin(Permanent lita) {
        harness.activateAbility(player1, battlefieldIndex(lita), null, null);
        harness.passBothPriorities();
        return findZeppelin();
    }

    private Permanent findZeppelin() {
        return findPermanent(player1, "Zeppelin");
    }

    private Permanent addLitaReady(Player player) {
        return addCreatureReady(player, new LitaMechanicalEngineer());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addLitaMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
