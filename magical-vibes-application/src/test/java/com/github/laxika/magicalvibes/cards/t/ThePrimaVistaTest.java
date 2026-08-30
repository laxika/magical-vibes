package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePrimaVista.class, GrizzlyBears.class, Hurricane.class})
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

    private Permanent addPrimaVistaReady(Player player) {
        Permanent primaVista = new Permanent(new ThePrimaVista());
        primaVista.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(primaVista);
        return primaVista;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
