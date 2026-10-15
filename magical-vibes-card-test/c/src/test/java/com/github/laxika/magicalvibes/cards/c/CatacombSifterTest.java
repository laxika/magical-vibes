package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    @Test
    void freshlyCreatedTappedScionCanBeSacrificedForManaAndScryToBottom() {
        harness.enterBattlefieldAndReturn(player1, new CatacombSifter());
        harness.passBothPriorities();
        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        scion.tap();
        Card topCard = new CatacombSifter();
        Card secondCard = new CatacombSifter();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
    }

    @Test
    void ownDeathDoesNotCauseScry() {
        Permanent sifter = harness.enterBattlefieldAndReturn(player1, new CatacombSifter());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new CatacombSifter()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, sifter));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Catacomb Sifter");
    }

    @Test
    void simultaneousDeathWithScionStillCausesOneScry() {
        Permanent sifter = harness.enterBattlefieldAndReturn(player1, new CatacombSifter());
        harness.passBothPriorities();
        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        Card topCard = new CatacombSifter();
        harness.setLibrary(player1, List.of(topCard));
        sifter.setMarkedDamage(3);
        scion.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Catacomb Sifter");
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        harness.passBothPriorities();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificingScionWithEmptyLibraryCompletesWithoutChoice() {
        harness.enterBattlefieldAndReturn(player1, new CatacombSifter());
        harness.passBothPriorities();
        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        harness.setLibrary(player1, List.of());

        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
