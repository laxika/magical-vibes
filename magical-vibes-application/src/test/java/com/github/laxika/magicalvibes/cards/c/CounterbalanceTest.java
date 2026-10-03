package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.cards.r.RonomHulk;
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

@CardUsed({Counterbalance.class, KjeldoranOutrider.class, RonomHulk.class, RiteOfFlame.class,
        HangarbackWalker.class})
class CounterbalanceTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger counters an opponent's spell when the mana values match")
    void matchingManaValueCountersSpell() {
        harness.addToBattlefield(player1, new Counterbalance());
        Card topCard = new KjeldoranOutrider();
        KjeldoranOutrider spell = new KjeldoranOutrider();
        harness.setLibrary(player1, List.of(topCard));
        castOpponentSpell(spell, "{1}{W}");

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(card -> card.getId())
                .isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("A nonmatching mana value leaves the opponent's spell to resolve")
    void nonmatchingManaValueDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new KjeldoranOutrider()));
        RonomHulk spell = new RonomHulk();
        castOpponentSpell(spell, "{4}{G}");

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Declining the trigger leaves the opponent's spell to resolve")
    void decliningDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new KjeldoranOutrider()));
        KjeldoranOutrider spell = new KjeldoranOutrider();
        castOpponentSpell(spell, "{1}{W}");

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Accepting the trigger counters a matching noncreature spell")
    void matchingManaValueCountersNoncreatureSpell() {
        harness.addToBattlefield(player1, new Counterbalance());
        RiteOfFlame topCard = new RiteOfFlame();
        RiteOfFlame spell = new RiteOfFlame();
        harness.setLibrary(player1, List.of(topCard));
        castOpponentSpell(spell, "{R}");

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(card -> card.getId())
                .isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("Accepting the trigger does not counter a spell when the library is empty")
    void emptyLibraryDoesNotCounterSpell() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of());
        KjeldoranOutrider spell = new KjeldoranOutrider();
        castOpponentSpell(spell, "{1}{W}");

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("The ability does not trigger for a spell cast by its controller")
    void controllerSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new KjeldoranOutrider(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @CardUsed({Counterbalance.class, KjeldoranOutrider.class, HangarbackWalker.class})
    @DisplayName("Both X symbols contribute to the mana value of the triggering spell")
    void doubleXManaValueMatchesTwoManaCard() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new KjeldoranOutrider()));
        HangarbackWalker spell = castOpponentWalkerForOne();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @CardUsed({Counterbalance.class, RiteOfFlame.class, HangarbackWalker.class})
    @DisplayName("A double-X spell with X=1 does not match a one-mana card")
    void doubleXManaValueDoesNotMatchOneManaCard() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new RiteOfFlame()));
        HangarbackWalker spell = castOpponentWalkerForOne();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spell.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("The revealed card is the library's top card when the ability resolves")
    void usesTopCardAtResolution() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new RonomHulk()));
        KjeldoranOutrider spell = new KjeldoranOutrider();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, spell, "{1}{W}");
        KjeldoranOutrider newTopCard = new KjeldoranOutrider();
        harness.setLibrary(player1, List.of(newTopCard));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .extracting(card -> card.getId())
                .isEqualTo(newTopCard.getId());
    }

    @Test
    @DisplayName("The reveal decision waits until the triggered ability resolves")
    void revealChoiceIsMadeAtResolution() {
        harness.addToBattlefield(player1, new Counterbalance());
        harness.setLibrary(player1, List.of(new KjeldoranOutrider()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new KjeldoranOutrider(), "{1}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private HangarbackWalker castOpponentWalkerForOne() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        HangarbackWalker spell = new HangarbackWalker();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);
        gs.playCard(gd, player2, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        return spell;
    }

    private void castOpponentSpell(Card card, String manaCost) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, card, manaCost);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
