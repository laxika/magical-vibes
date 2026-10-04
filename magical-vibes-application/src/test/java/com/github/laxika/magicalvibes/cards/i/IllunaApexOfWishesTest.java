package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllunaApexOfWishes.class, Forest.class, GrizzlyBears.class, Shock.class, Pacifism.class})
class IllunaApexOfWishesTest extends BaseCardTest {

    @Test
    @DisplayName("A mutation exiles until a nonland permanent and puts it onto the battlefield")
    void mutationPutsFoundPermanentOntoBattlefield() {
        Permanent illuna = addCreatureReady(player1, new IllunaApexOfWishes());
        Card shock = new Shock();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, forest, bears));

        triggerMutation(illuna);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shock.getId(), forest.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(illuna.getCard().getId(), bears.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the battlefield choice puts the found permanent into hand")
    void decliningPutsFoundPermanentIntoHand() {
        Permanent illuna = addCreatureReady(player1, new IllunaApexOfWishes());
        Card shock = new Shock();
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, forest, bears));

        triggerMutation(illuna);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shock.getId(), forest.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Cards exiled without finding a nonland permanent remain in exile")
    void noNonlandPermanentLeavesAllCardsInExile() {
        Permanent illuna = addCreatureReady(player1, new IllunaApexOfWishes());
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, forest));

        triggerMutation(illuna);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shock.getId(), forest.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library produces no destination choice")
    void emptyLibraryDoesNothing() {
        Permanent illuna = addCreatureReady(player1, new IllunaApexOfWishes());
        harness.setLibrary(player1, List.of());

        triggerMutation(illuna);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Illuna, Apex of Wishes");
    }

    @Test
    @DisplayName("The first nonland permanent stops the exile sequence")
    void stopsAtFirstNonlandPermanent() {
        Permanent illuna = addCreatureReady(player1, new IllunaApexOfWishes());
        Card pacifism = new Pacifism();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(pacifism, forest));

        triggerMutation(illuna);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Pacifism");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("An Aura entering from exile requires a legal attachment choice")
    void auraEntersAttachedToChosenCreature() {
        Permanent illuna = addCreatureReady(player1, new IllunaApexOfWishes());
        addCreatureReady(player2, new IllunaApexOfWishes());
        harness.setLibrary(player1, List.of(new Pacifism()));

        triggerMutation(illuna);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, illuna.getId());

        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(illuna.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Aura with no legal attachment remains in exile")
    void auraWithoutLegalAttachmentRemainsExiled() {
        Permanent illuna = addCreatureReady(player1, new IllunaApexOfWishes());
        Card pacifism = new Pacifism();
        harness.setLibrary(player1, List.of(pacifism));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, illuna, List.of(illuna.getCard()), player1.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(pacifism.getId());
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
        harness.assertNotInHand(player1, "Pacifism");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void triggerMutation(Permanent illuna) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, illuna, List.of(illuna.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
