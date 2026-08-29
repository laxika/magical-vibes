package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FranticScapegoat.class, GrizzlyBears.class})
class FranticScapegoatTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldSuspected() {
        harness.setHand(player1, List.of(new FranticScapegoat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Frantic Scapegoat").isSuspected()).isTrue();
    }

    @Test
    void maySuspectOneOtherCreatureAndClearItself() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        castGrizzlyBears();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isSuspected()).isTrue();
        assertThat(scapegoat.isSuspected()).isFalse();
    }

    @Test
    void choosingAmongOtherCreaturesExcludesTheScapegoat() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        Permanent existingBears = addCreatureReady(player1, new GrizzlyBears());
        castGrizzlyBears();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SuspectChosenOtherCreature.class);
        harness.handlePermanentChosen(player1, existingBears.getId());

        Permanent enteringBears = findPermanents(player1, "Grizzly Bears").getLast();
        assertThat(existingBears.isSuspected()).isTrue();
        assertThat(enteringBears.isSuspected()).isFalse();
        assertThat(scapegoat.isSuspected()).isFalse();
    }

    @Test
    @CardUsed(CallTheCavalry.class)
    void tokenBatchCreatesOneMayTrigger() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        harness.setHand(player1, List.of(new CallTheCavalry()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent chosenKnight = findPermanents(player1, "Knight").getFirst();
        harness.handlePermanentChosen(player1, chosenKnight.getId());

        assertThat(chosenKnight.isSuspected()).isTrue();
        assertThat(scapegoat.isSuspected()).isFalse();
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
