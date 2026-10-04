package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AbandonAttachments;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FatedFirepower;
import com.github.laxika.magicalvibes.cards.f.ForecastingFortuneTeller;
import com.github.laxika.magicalvibes.cards.g.GranGran;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JetsBrainwashing;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HamaTheBloodbender.class, Divination.class, GrizzlyBears.class, Swamp.class,
        AbandonAttachments.class, ForecastingFortuneTeller.class, GranGran.class, JetsBrainwashing.class, FatedFirepower.class})
class HamaTheBloodbenderTest extends BaseCardTest {

    @Test
    void millsAndMayExileAnEligibleCardFromTheTargetPlayersGraveyard() {
        Card eligible = new Divination();
        Card creature = new GrizzlyBears();
        Card land = new Swamp();
        Card firstMilled = new GrizzlyBears();
        Card secondMilled = new GrizzlyBears();
        Card thirdMilled = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(eligible, creature, land));
        harness.setLibrary(player2, List.of(firstMilled, secondMilled, thirdMilled));
        harness.setHand(player1, List.of(new HamaTheBloodbender()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        assertThat(choice.minCount()).isZero();

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        Permanent hama = findPermanent(player1, "Hama, the Bloodbender");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSize(5)
                .contains(creature, land)
                .contains(firstMilled, secondMilled, thirdMilled)
                .doesNotContain(eligible);
        assertThat(gd.getCardsExiledByPermanent(hama.getId())).containsExactly(eligible);
    }

    @Test
    void castsATrackedCardByWaterbendingItsManaValue() {
        Permanent hama = addCreatureReady(player1, new HamaTheBloodbender());
        Permanent firstContributor = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondContributor = addCreatureReady(player1, new GrizzlyBears());
        Permanent thirdContributor = addCreatureReady(player1, new GrizzlyBears());
        Card exiledCard = new Divination();
        gd.addToExile(player2.getId(), exiledCard, hama.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castFromExileWithWaterbend(player1, exiledCard.getId(),
                List.of(firstContributor.getId(), secondContributor.getId(), thirdContributor.getId()));
        harness.passBothPriorities();

        assertThat(firstContributor.isTapped()).isTrue();
        assertThat(secondContributor.isTapped()).isTrue();
        assertThat(thirdContributor.isTapped()).isTrue();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiledCard);
    }

    @Test
    void losesThePermissionWhenHamaLeavesTheBattlefield() {
        Permanent hama = addCreatureReady(player1, new HamaTheBloodbender());
        Card exiledCard = new Divination();
        gd.addToExile(player2.getId(), exiledCard, hama.getId());
        gd.playerBattlefields.get(player1.getId()).remove(hama);

        assertThatThrownBy(() -> harness.castFromExileWithWaterbend(player1, exiledCard.getId(), List.of()))
                .hasMessageContaining("permission");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    @Test
    void cannotPayTheNormalManaCostInsteadOfWaterbending() {
        Permanent hama = addCreatureReady(player1, new HamaTheBloodbender());
        Card exiledCard = new Divination();
        gd.addToExile(player2.getId(), exiledCard, hama.getId());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCard.getId()))
                .hasMessageContaining("permission");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
    }

    @Test
    void canExileACardJustMilledByHama() {
        Card eligible = new AbandonAttachments();
        harness.setLibrary(player2, List.of(eligible, new ForecastingFortuneTeller(), new Swamp()));
        harness.setHand(player1, List.of(new HamaTheBloodbender()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        Permanent hama = findPermanent(player1, "Hama, the Bloodbender");
        assertThat(gd.getCardsExiledByPermanent(hama.getId())).containsExactly(eligible);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void mayDeclineToExileAnEligibleCard() {
        Card eligible = new AbandonAttachments();
        castHamaWithGraveyardCard(eligible);

        harness.handleMultipleCardsChosen(player1, List.of());

        Permanent hama = findPermanent(player1, "Hama, the Bloodbender");
        assertThat(gd.getCardsExiledByPermanent(hama.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(eligible).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutAChoiceWhenNoEligibleCardsExist() {
        harness.setLibrary(player2, List.of(new ForecastingFortuneTeller(), new Swamp()));
        harness.setHand(player1, List.of(new HamaTheBloodbender()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCombineManaAndASummoningSickCreatureForWaterbending() {
        Card exiled = new AbandonAttachments();
        enterHamaAndExile(exiled);
        Permanent contributor = harness.addToBattlefieldAndReturn(player1, new ForecastingFortuneTeller());
        contributor.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromExileWithWaterbend(player1, exiled.getId(), List.of(contributor.getId()));

        assertThat(contributor.isTapped()).isTrue();
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotCastTheExiledInstantDuringAnOpponentsTurn() {
        Card exiled = new AbandonAttachments();
        enterHamaAndExile(exiled);
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExileWithWaterbend(player1, exiled.getId(), List.of()))
                .hasMessageContaining("permission");
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void appliesCostReductionsToTheWaterbendAlternativeCost() {
        Card exiled = new AbandonAttachments();
        Permanent hama = enterHamaAndExile(exiled);
        Permanent granGran = addCreatureReady(player1, new GranGran());
        harness.setGraveyard(player1, List.of(
                new AbandonAttachments(), new AbandonAttachments(), new AbandonAttachments()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExileWithWaterbend(player1, exiled.getId(), List.of());

        assertThat(gd.findExiledCard(exiled.getId())).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(hama.isTapped()).isFalse();
        assertThat(granGran.isTapped()).isFalse();
    }

    @Test
    void stealingHamaDoesNotTransferTheExiledCardPermission() {
        Card exiled = new AbandonAttachments();
        Permanent hama = enterHamaAndExile(exiled);
        stealHama(hama);
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExileWithWaterbend(player2, exiled.getId(), List.of()))
                .hasMessageContaining("permission");
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void regainingHamaDoesNotRestartTheExpiredPermission() {
        Card exiled = new AbandonAttachments();
        Permanent hama = enterHamaAndExile(exiled);
        stealHama(hama);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hama);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExileWithWaterbend(player1, exiled.getId(), List.of()))
                .hasMessageContaining("permission");
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void mustChooseZeroForXInTheExiledSpellsManaCost() {
        Card exiled = new FatedFirepower();
        enterHamaAndExile(exiled);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, exiled.getId(), 5, null,
                List.of(), List.of(), List.of(), true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(exiled.getId())).isNotNull();
    }

    @Test
    void canCastAnXSpellWithXZeroByWaterbendingItsManaValue() {
        Card exiled = new FatedFirepower();
        enterHamaAndExile(exiled);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castFromExileWithWaterbend(player1, exiled.getId(), List.of());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Fated Firepower").getCounterCount(CounterType.FIRE)).isZero();
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
    }

    private void castHamaWithGraveyardCard(Card card) {
        harness.setGraveyard(player2, List.of(card));
        harness.setLibrary(player2, List.of(
                new ForecastingFortuneTeller(), new ForecastingFortuneTeller(), new ForecastingFortuneTeller()));
        harness.setHand(player1, List.of(new HamaTheBloodbender()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
    }

    private Permanent enterHamaAndExile(Card card) {
        castHamaWithGraveyardCard(card);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        return findPermanent(player1, "Hama, the Bloodbender");
    }

    private void stealHama(Permanent hama) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new JetsBrainwashing()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castKickedInstant(player2, 0, hama.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hama);
    }
}
