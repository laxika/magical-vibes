package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoleModule.class, GrizzlyBears.class, Shock.class})
class MoleModuleTest extends BaseCardTest {

    @Test
    void crewTwoAnimatesMoleModule() {
        Permanent mole = addMoleModuleReady();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mole)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void combatDamageMillsFourAndMayPutPermanentOntoBattlefield() {
        Permanent mole = addAttackingMoleModule();
        Card permanent = new GrizzlyBears();
        harness.setLibrary(player1, List.of(permanent, new Shock(), new Shock(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).hasSize(1);

        harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(mole.isAttacking()).isFalse();
    }

    @Test
    void mayDeclinePuttingMilledPermanentOntoBattlefield() {
        addAttackingMoleModule();
        Card permanent = new GrizzlyBears();
        harness.setLibrary(player1, List.of(permanent, new Shock(), new Shock(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(permanent);
    }

    private Permanent addMoleModuleReady() {
        Permanent mole = new Permanent(new MoleModule());
        mole.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mole);
        return mole;
    }

    private Permanent addAttackingMoleModule() {
        Permanent mole = addMoleModuleReady();
        mole.setAnimatedUntilEndOfTurn(true);
        mole.setAnimatedPower(6);
        mole.setAnimatedToughness(6);
        mole.setAttacking(true);
        return mole;
    }
}
