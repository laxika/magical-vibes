package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AcademyRuins;
import com.github.laxika.magicalvibes.cards.a.AmrouScout;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.o.OpalGuardian;
import com.github.laxika.magicalvibes.model.Card;
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
        ChromaticStar.class, OpalGuardian.class})
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
        AcademyRuins secondRuins = new AcademyRuins();
        Cancel cancel = new Cancel();
        suspendCard(List.of(scout, firstRuins, cancel));
        harness.setHand(player2, List.of(secondRuins, new AcademyRuins()));

        resolveSuspendedHypergenesis();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMultipleCardsChosen(player2, List.of(secondRuins.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Academy Ruins");

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
                .containsExactly("Academy Ruins", "Academy Ruins");
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
                .hasMessageContaining("sorcery speed");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
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
