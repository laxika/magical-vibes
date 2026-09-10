package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CatacombSifter.class, GrizzlyBears.class})
class CatacombSifterTest extends BaseCardTest {

    @Test
    void enteringCreatesEldraziScionToken() {
        harness.enterBattlefieldAndReturn(player1, new CatacombSifter());
        harness.passBothPriorities();

        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        assertThat(scion.getCard().isToken()).isTrue();
        assertThat(scion.getCard().getSubtypes()).containsExactly(CardSubtype.ELDRAZI, CardSubtype.SCION);
        assertThat(scion.getEffectivePower()).isEqualTo(1);
        assertThat(scion.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void anotherCreatureYouControlDyingCausesScry() {
        harness.enterBattlefieldAndReturn(player1, new CatacombSifter());
        harness.passBothPriorities();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, bears));
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void opponentCreatureDyingDoesNotCauseScry() {
        harness.enterBattlefieldAndReturn(player1, new CatacombSifter());
        harness.passBothPriorities();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
