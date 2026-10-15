package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArachnePsionicWeaver;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpectacularSpiderMan;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadioactiveSpider.class, ArachnePsionicWeaver.class, SpectacularSpiderMan.class,
        GiantSpider.class, GrizzlyBears.class})
class RadioactiveSpiderTest extends BaseCardTest {

    @Test
    void sacrificesAndOffersOnlySpiderHeroes() {
        activateAbility(List.of(
                new ArachnePsionicWeaver(),
                new GiantSpider(),
                new SpectacularSpiderMan(),
                new GrizzlyBears()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Radioactive Spider");
        harness.assertInGraveyard(player1, "Radioactive Spider");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Arachne, Psionic Weaver", "Spectacular Spider-Man");
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    void chosenSpiderHeroGoesToHand() {
        activateAbility(List.of(new ArachnePsionicWeaver(), new GiantSpider()));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Arachne, Psionic Weaver");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void abilityRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new RadioactiveSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void sacrificeIsPaidBeforeAbilityResolves() {
        activateAbility(List.of(new ArachnePsionicWeaver()));

        harness.assertNotOnBattlefield(player1, "Radioactive Spider");
        harness.assertInGraveyard(player1, "Radioactive Spider");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Arachne, Psionic Weaver");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canFailToFindEvenWhenSpiderHeroIsAvailable() {
        activateAbility(List.of(new ArachnePsionicWeaver()));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertNotInHand(player1, "Arachne, Psionic Weaver");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Arachne, Psionic Weaver");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void searchWithNoSpiderHeroCompletesWithoutFindingCard() {
        activateAbility(List.of(new RadioactiveSpider()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Radioactive Spider");
        harness.assertNotInHand(player1, "Radioactive Spider");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void emptyLibraryDoesNotPreventActivation() {
        activateAbility(List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Radioactive Spider");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    void deathtouchDestroysCreatureWithMoreThanOneToughness() {
        addCreatureReady(player1, new RadioactiveSpider());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Radioactive Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new SpectacularSpiderMan());
        var spider = addCreatureReady(player2, new RadioactiveSpider());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new RadioactiveSpider());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Radioactive Spider");
        harness.assertNotInGraveyard(player1, "Radioactive Spider");
    }

    @Test
    void cannotActivateWithAbilityAlreadyOnStack() {
        harness.addToBattlefield(player1, new RadioactiveSpider());
        harness.addToBattlefield(player1, new RadioactiveSpider());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new ArachnePsionicWeaver()));
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(countPermanents(player1, "Radioactive Spider")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void insufficientManaDoesNotSacrificeSpider() {
        harness.addToBattlefield(player1, new RadioactiveSpider());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Radioactive Spider");
        harness.assertNotInGraveyard(player1, "Radioactive Spider");
        assertThat(gd.stack).isEmpty();
    }

    private void activateAbility(List<com.github.laxika.magicalvibes.model.Card> library) {
        harness.addToBattlefield(player1, new RadioactiveSpider());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.activateAbility(player1, 0, null, null);
    }
}
