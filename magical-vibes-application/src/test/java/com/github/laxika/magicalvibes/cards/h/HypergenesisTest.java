package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AcademyRuins;
import com.github.laxika.magicalvibes.cards.a.AmrouScout;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.o.OpalGuardian;
import com.github.laxika.magicalvibes.cards.p.ParadisePlume;
import com.github.laxika.magicalvibes.cards.s.SageOfEpityr;
import com.github.laxika.magicalvibes.cards.t.TemporalIsolation;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hypergenesis.class, AcademyRuins.class, AmrouScout.class, Cancel.class,
        ChromaticStar.class, OpalGuardian.class, ParadisePlume.class, SageOfEpityr.class,
        TemporalIsolation.class, TerramorphicExpanse.class})
class HypergenesisTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Hypergenesis with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        Hypergenesis card = suspendCard(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("Repeats after a decline and puts permanents onto the battlefield sequentially")
    void repeatsAfterDeclineAndEntersPermanentsSequentially() {
        AmrouScout scout = new AmrouScout();
        AcademyRuins firstRuins = new AcademyRuins();
        TerramorphicExpanse secondLand = new TerramorphicExpanse();
        Cancel cancel = new Cancel();
        suspendCard(List.of(scout, firstRuins, cancel));
        harness.setHand(player2, List.of(secondLand, new TerramorphicExpanse()));

        resolveSuspendedHypergenesis();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player2, List.of(secondLand.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Terramorphic Expanse");

        harness.handleMultipleCardsChosen(player1, List.of(scout.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Amrou Scout");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);

        harness.handleMultipleCardsChosen(player2, List.of(gd.playerHands.get(player2.getId()).getFirst().getId()));
        harness.handleMultipleCardsChosen(player1, List.of(firstRuins.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Amrou Scout", "Academy Ruins");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Terramorphic Expanse", "Terramorphic Expanse");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Cancel");
    }

    @Test
    @DisplayName("Accepts artifact, creature, enchantment, and land cards")
    void acceptsEveryPermanentCardType() {
        ChromaticStar artifact = new ChromaticStar();
        AmrouScout creature = new AmrouScout();
        OpalGuardian enchantment = new OpalGuardian();
        AcademyRuins land = new AcademyRuins();
        List<Card> permanents = List.of(artifact, creature, enchantment, land);
        suspendCard(permanents);
        harness.setHand(player2, List.of());

        resolveSuspendedHypergenesis();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validCardIds()).containsExactly(
                artifact.getId(), creature.getId(), enchantment.getId(), land.getId());

        for (Card permanent : permanents) {
            harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Chromatic Star", "Amrou Scout", "Opal Guardian", "Academy Ruins");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when no player has an eligible permanent")
    void doesNothingWithoutEligibleCards() {
        Cancel cancel = new Cancel();
        suspendCard(List.of(cancel));
        harness.setHand(player2, List.of());

        resolveSuspendedHypergenesis();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Cancel");
        harness.assertInGraveyard(player1, "Hypergenesis");
    }

    @Test
    @DisplayName("Suspend can only be activated at sorcery speed")
    void suspendRequiresSorcerySpeed() {
        Hypergenesis card = new Hypergenesis();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be suspended at this time");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("Stops when both players decline even with eligible cards remaining")
    void stopsWhenEveryoneDeclines() {
        AmrouScout scout = new AmrouScout();
        ChromaticStar star = new ChromaticStar();
        suspendCard(List.of(scout));
        harness.setHand(player2, List.of(star));

        resolveSuspendedHypergenesis();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Amrou Scout");
        harness.assertInHand(player2, "Chromatic Star");
        harness.assertNotOnBattlefield(player1, "Amrou Scout");
        harness.assertNotOnBattlefield(player2, "Chromatic Star");
        harness.assertInGraveyard(player1, "Hypergenesis");
    }

    @Test
    @DisplayName("Each choice permits only one eligible card")
    void rejectsMultipleCardsAndInstants() {
        AmrouScout scout = new AmrouScout();
        ChromaticStar star = new ChromaticStar();
        Cancel cancel = new Cancel();
        suspendCard(List.of(scout, star, cancel));
        harness.setHand(player2, List.of());
        resolveSuspendedHypergenesis();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(scout.getId(), star.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(cancel.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(scout, star, cancel);
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An Aura attaches to a creature put onto the battlefield in an earlier round")
    void auraAttachesToEarlierCreature() {
        AmrouScout scout = new AmrouScout();
        TemporalIsolation aura = new TemporalIsolation();
        suspendCard(List.of(scout, aura));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new AmrouScout());
        resolveSuspendedHypergenesis();

        harness.handleMultipleCardsChosen(player1, List.of(scout.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Amrou Scout").getId());

        assertThat(findPermanent(player1, "Temporal Isolation").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Amrou Scout").getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An Aura with nothing legal to enchant remains in hand")
    void auraWithoutLegalAttachmentRemainsInHand() {
        TemporalIsolation aura = new TemporalIsolation();
        suspendCard(List.of(aura));
        harness.setHand(player2, List.of());
        resolveSuspendedHypergenesis();

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        harness.assertNotOnBattlefield(player1, "Temporal Isolation");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Entry triggers wait until the entire Hypergenesis process finishes")
    void entryTriggersResolveAfterAllChoices() {
        SageOfEpityr sage = new SageOfEpityr();
        ChromaticStar star = new ChromaticStar();
        suspendCard(List.of(sage, star));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new AmrouScout(), new ChromaticStar(),
                new Cancel(), new AcademyRuins()));
        resolveSuspendedHypergenesis();

        harness.handleMultipleCardsChosen(player1, List.of(sage.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(star.getId()));
        harness.assertOnBattlefield(player1, "Chromatic Star");
        harness.assertInGraveyard(player1, "Hypergenesis");
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        assertThat(reorder.cards()).hasSize(4);
    }

    @Test
    @DisplayName("As-entry color choices happen before the next permanent choice")
    void choosesColorBeforeContinuingProcess() {
        ParadisePlume plume = new ParadisePlume();
        ChromaticStar star = new ChromaticStar();
        suspendCard(List.of(plume, star));
        harness.setHand(player2, List.of());
        resolveSuspendedHypergenesis();

        harness.handleMultipleCardsChosen(player1, List.of(plume.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");
        assertThat(findPermanent(player1, "Paradise Plume").getChosenColor()).isEqualTo(CardColor.GREEN);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(star.getId()));
        harness.assertOnBattlefield(player1, "Chromatic Star");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Hypergenesis suspendCard(List<Card> additionalHandCards) {
        Hypergenesis card = new Hypergenesis();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.setHand(player1, additionalHandCards);
        return card;
    }

    private void resolveSuspendedHypergenesis() {
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }
}
