package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingLightning.class, Murder.class, Opt.class, GrizzlyBears.class, LavaCoil.class})
class LivingLightningTest extends BaseCardTest {

    @Test
    void deathTriggerReturnsTargetInstantOrSorceryFromGraveyard() {
        Permanent livingLightning = harness.addToBattlefieldAndReturn(player1, new LivingLightning());
        Opt opt = new Opt();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(opt, bears));

        destroyLivingLightning(livingLightning);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opt.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opt.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Opt");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void deathTriggerDoesNotTargetCreatureCards() {
        Permanent livingLightning = harness.addToBattlefieldAndReturn(player1, new LivingLightning());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        destroyLivingLightning(livingLightning);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void deathTriggerReturnsSorceryAndExcludesOpponentsGraveyard() {
        Permanent livingLightning = harness.addToBattlefieldAndReturn(player1, new LivingLightning());
        LavaCoil lavaCoil = new LavaCoil();
        Opt opponentsOpt = new Opt();
        harness.setGraveyard(player1, List.of(lavaCoil));
        harness.setGraveyard(player2, List.of(opponentsOpt));

        destroyLivingLightning(livingLightning);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(lavaCoil.getId());
        harness.handleMultipleCardsChosen(player1, List.of(lavaCoil.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lava Coil");
        harness.assertNotInGraveyard(player1, "Lava Coil");
        harness.assertInGraveyard(player2, "Opt");
        harness.assertInGraveyard(player1, "Living Lightning");
    }

    @Test
    void deathTriggerHasNoLegalTargetWhenOnlyOpponentHasAnInstant() {
        Permanent livingLightning = harness.addToBattlefieldAndReturn(player1, new LivingLightning());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new Opt()));

        destroyLivingLightning(livingLightning);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
        harness.assertInGraveyard(player2, "Opt");
        harness.assertInGraveyard(player1, "Living Lightning");
    }

    private void destroyLivingLightning(Permanent livingLightning) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, livingLightning.getId());
    }
}
