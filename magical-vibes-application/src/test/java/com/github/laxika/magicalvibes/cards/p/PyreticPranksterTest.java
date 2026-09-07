package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlisteningGoremonger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        PyreticPrankster.class,
        GlisteningGoremonger.class,
        GrizzlyBears.class,
        Ornithopter.class,
        Forest.class
})
class PyreticPranksterTest extends BaseCardTest {

    @Test
    void transformsByPayingBlackMana() {
        Permanent prankster = addPrankster();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prankster.isTransformed()).isTrue();
        assertThat(prankster.getCard()).isInstanceOf(GlisteningGoremonger.class);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent prankster = addPrankster();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prankster.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void transformedFaceDeathTriggerMakesEachOpponentSacrificeAnArtifactOrCreature() {
        Permanent goremonger = addTransformedPrankster();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Forest());

        goremonger.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    private Permanent addPrankster() {
        return harness.addToBattlefieldAndReturn(player1, new PyreticPrankster());
    }

    private Permanent addTransformedPrankster() {
        Permanent prankster = addPrankster();
        prankster.setCard(prankster.getCard().getBackFaceCard());
        prankster.setTransformed(true);
        return prankster;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
