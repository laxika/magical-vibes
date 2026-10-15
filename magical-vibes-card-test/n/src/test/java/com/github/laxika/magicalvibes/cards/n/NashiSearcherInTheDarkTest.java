package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YomijiWhoBarsTheWay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NashiSearcherInTheDark.class, Pacifism.class, Shock.class, YomijiWhoBarsTheWay.class,
        BruvacTheGrandiloquent.class, LeylineOfTheVoid.class})
class NashiSearcherInTheDarkTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage mills that many cards and offers any number of legendary or enchantment cards")
    void millsDamageAmountAndReturnsAcceptedEligibleCards() {
        Permanent nashi = addAttackingNashi();
        nashi.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of());
        YomijiWhoBarsTheWay legendary = new YomijiWhoBarsTheWay();
        Pacifism enchantment = new Pacifism();
        Shock invalid = new Shock();
        harness.setLibrary(player1, List.of(legendary, enchantment, invalid));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(legendary, enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(invalid);
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Putting no milled eligible cards into hand puts a +1/+1 counter on Nashi")
    void declinesAllEligibleCardsAndGetsCounter() {
        Permanent nashi = addAttackingNashi();
        harness.setHand(player1, List.of());
        YomijiWhoBarsTheWay legendary = new YomijiWhoBarsTheWay();
        Pacifism enchantment = new Pacifism();
        harness.setLibrary(player1, List.of(legendary, enchantment));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(legendary, enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(legendary, enchantment);
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No eligible milled cards automatically put a +1/+1 counter on Nashi")
    void noEligibleCardsGetsCounter() {
        Permanent nashi = addAttackingNashi();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning only one of two eligible cards does not add a counter")
    void acceptsOneEligibleCardAndDeclinesTheOther() {
        Permanent nashi = addAttackingNashi();
        harness.setHand(player1, List.of());
        NashiSearcherInTheDark first = new NashiSearcherInTheDark();
        NashiSearcherInTheDark second = new NashiSearcherInTheDark();
        harness.setLibrary(player1, List.of(first, second));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An empty library still gives Nashi one counter")
    void emptyLibraryGetsCounter() {
        Permanent nashi = addAttackingNashi();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger still mills and returns cards after Nashi is destroyed")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent nashi = addAttackingNashi();
        harness.setHand(player1, List.of());
        NashiSearcherInTheDark first = new NashiSearcherInTheDark();
        NashiSearcherInTheDark second = new NashiSearcherInTheDark();
        harness.setLibrary(player1, List.of(first, second));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, nashi.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Nashi, Searcher in the Dark");
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nashi.getCard());
    }

    @Test
    @DisplayName("All eligible cards can be returned when Bruvac doubles the mill")
    void canReturnMoreCardsThanDamageDealt() {
        Permanent nashi = addAttackingNashi();
        harness.addToBattlefield(player2, new BruvacTheGrandiloquent());
        harness.setHand(player1, List.of());
        List<NashiSearcherInTheDark> cards = List.of(new NashiSearcherInTheDark(),
                new NashiSearcherInTheDark(), new NashiSearcherInTheDark(), new NashiSearcherInTheDark());
        harness.setLibrary(player1, cards);

        resolveCombat();
        harness.passBothPriorities();
        for (int i = 0; i < cards.size(); i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Eligible milled cards exiled by Leyline can still be put into hand")
    void canReturnCardsMilledIntoExile() {
        Permanent nashi = addAttackingNashi();
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setHand(player1, List.of());
        NashiSearcherInTheDark first = new NashiSearcherInTheDark();
        NashiSearcherInTheDark second = new NashiSearcherInTheDark();
        harness.setLibrary(player1, List.of(first, second));

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addAttackingNashi() {
        Permanent nashi = addCreatureReady(player1, new NashiSearcherInTheDark());
        nashi.setAttacking(true);
        return nashi;
    }
}
