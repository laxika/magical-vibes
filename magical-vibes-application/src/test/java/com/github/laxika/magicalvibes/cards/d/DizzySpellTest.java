package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.c.CompulsiveResearch;
import com.github.laxika.magicalvibes.cards.l.LoreBroker;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DizzySpell.class, Darkblast.class, BorosSignet.class, CompulsiveResearch.class,
        LoreBroker.class, Watchwolf.class})
class DizzySpellTest extends BaseCardTest {

    @Test
    void givesTargetCreatureMinusThreePower() {
        harness.addToBattlefield(player2, new Watchwolf());
        harness.setHand(player1, List.of(new DizzySpell()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Watchwolf"));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Watchwolf"))).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Watchwolf"))).isEqualTo(3);
    }

    @Test
    void spellBoostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new Watchwolf());
        harness.setHand(player1, List.of(new DizzySpell()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Watchwolf"));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Watchwolf"))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Watchwolf"))).isEqualTo(3);
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        DizzySpell dizzySpell = new DizzySpell();
        Darkblast matchingCard = new Darkblast();
        BorosSignet differentManaValue = new BorosSignet();
        CompulsiveResearch anotherDifferentManaValue = new CompulsiveResearch();
        harness.setHand(player1, List.of(dizzySpell));
        harness.setLibrary(player1, List.of(matchingCard, differentManaValue, anotherDifferentManaValue));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Dizzy Spell");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(differentManaValue, anotherDifferentManaValue);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        harness.setHand(player1, List.of(new DizzySpell()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void transmuteKeepsItsSourceManaValueWhenAnotherCardIsDiscardedInResponse() {
        DizzySpell dizzySpell = new DizzySpell();
        Darkblast matchingCard = new Darkblast();
        BorosSignet differentManaValue = new BorosSignet();
        LoreBroker loreBroker = new LoreBroker();
        Permanent broker = addCreatureReady(player2, loreBroker);

        harness.setHand(player1, List.of(dizzySpell));
        harness.setLibrary(player1, List.of(new CompulsiveResearch(), matchingCard, differentManaValue));
        harness.setHand(player2, List.of(new CompulsiveResearch()));
        harness.setLibrary(player2, List.of(new BorosSignet()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(broker), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search)
                .as("the transmute search should still use Dizzy Spell's mana value after a response")
                .isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCard);
    }

    @Test
    void spellCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new BorosSignet());
        harness.setHand(player1, List.of(new DizzySpell()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Boros Signet")))
                .isInstanceOf(IllegalStateException.class);
    }
}
