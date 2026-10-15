package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.k.KavuClimber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePrimaVista.class, GrizzlyBears.class, Hurricane.class, KavuClimber.class})
class ThePrimaVistaTest extends BaseCardTest {

    @Test
    void fourManaNoncreatureSpellAnimatesPrimaVista() {
        Permanent primaVista = addPrimaVistaReady(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, primaVista)).isTrue();
        assertThat(primaVista.isAnimatedUntilEndOfTurn()).isTrue();
    }

    @Test
    void fewerThanFourManaDoesNotAnimatePrimaVista() {
        Permanent primaVista = addPrimaVistaReady(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, primaVista)).isFalse();
    }

    @Test
    void crewAnimatesPrimaVistaAndTapsCrew() {
        Permanent primaVista = addPrimaVistaReady(player1);
        Permanent crew = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, primaVista)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void creatureSpellDoesNotAnimatePrimaVista() {
        Permanent primaVista = addPrimaVistaReady(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, primaVista)).isFalse();
    }

    @Test
    void fiveManaCreatureSpellDoesNotAnimatePrimaVista() {
        Permanent primaVista = addPrimaVistaReady(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new KavuClimber()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, primaVista)).isFalse();
    }

    @Test
    void opponentsFourManaSpellDoesNotAnimatePrimaVista() {
        Permanent primaVista = addPrimaVistaReady(player1);
        setUpMainPhase(player2);

        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Hurricane()));
        harness.castSorcery(player2, 0, 3);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, primaVista)).isFalse();
        harness.assertOnBattlefield(player1, "The Prima Vista");
    }

    @Test
    void animationResolvesBeforeTheSpellThatTriggeredIt() {
        Permanent primaVista = addPrimaVistaReady(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, primaVista)).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Prima Vista");
        harness.assertInGraveyard(player1, "The Prima Vista");
    }

    @Test
    void summoningSickCreatureCanCrewPrimaVista() {
        Permanent primaVista = addPrimaVistaReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, primaVista)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void crewAnimationExpiresAtEndOfTurn() {
        Permanent primaVista = addPrimaVistaReady(player1);
        addCreatureReady(player1);
        setUpMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, primaVista)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, primaVista)).isFalse();
        harness.assertOnBattlefield(player1, "The Prima Vista");
    }

    private Permanent addPrimaVistaReady(Player player) {
        Permanent primaVista = harness.addToBattlefieldAndReturn(player, new ThePrimaVista());
        primaVista.setSummoningSick(false);
        return primaVista;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
