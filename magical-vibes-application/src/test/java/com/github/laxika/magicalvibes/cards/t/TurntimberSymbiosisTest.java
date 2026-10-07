package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KazanduMammoth;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TurntimberSymbiosis.class, TurntimberSerpentineWood.class,
        GrizzlyBears.class, ColossalDreadmaw.class, Shock.class, KazanduMammoth.class})
class TurntimberSymbiosisTest extends BaseCardTest {

    @Test
    void putsLowManaValueCreatureOntoBattlefieldWithCounters() {
        Card chosen = new GrizzlyBears();
        Card expensive = new ColossalDreadmaw();
        setLibrary(chosen, new Shock(), expensive, new Shock(), new Shock(), new Shock(), new Shock());
        castSymbiosis();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), expensive.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        Permanent entered = findPermanent(player1, chosen.getName());
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void putsHighManaValueCreatureOntoBattlefieldWithoutCounters() {
        Card chosen = new ColossalDreadmaw();
        setLibrary(chosen, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        castSymbiosis();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        Permanent entered = findPermanent(player1, chosen.getName());
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void landFaceMayEnterUntappedForThreeLifeAndProducesGreenMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TurntimberSymbiosis()));

        gs.playCard(gd, player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(TurntimberSerpentineWood.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(land.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void landFaceEntersTappedWhenLifePaymentIsDeclined() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TurntimberSymbiosis()));

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void creatureWithManaValueExactlyThreeEntersOnItsFrontFaceWithCounters() {
        Card chosen = new KazanduMammoth();
        setLibrary(chosen);
        castSymbiosis();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        Permanent entered = findPermanent(player1, chosen.getName());
        assertThat(entered.getCard()).isInstanceOf(KazanduMammoth.class);
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDeclineCreatureAndBottomAllLookedAtCards() {
        Card creature = new GrizzlyBears();
        Card other = new TurntimberSymbiosis();
        setLibrary(creature, other);
        castSymbiosis();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void onlyLooksAtSevenCardsAndLeavesUntouchedCardsAboveTheRest() {
        Card chosen = new GrizzlyBears();
        List<Card> rest = List.of(new TurntimberSymbiosis(), new TurntimberSymbiosis(),
                new TurntimberSymbiosis(), new TurntimberSymbiosis(),
                new TurntimberSymbiosis(), new TurntimberSymbiosis());
        Card eighth = new GrizzlyBears();
        Card ninth = new TurntimberSymbiosis();
        setLibrary(chosen, rest.get(0), rest.get(1), rest.get(2), rest.get(3),
                rest.get(4), rest.get(5), eighth, ninth);
        castSymbiosis();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(8);
        assertThat(library.subList(0, 2)).containsExactly(eighth, ninth);
        assertThat(library.subList(2, 8)).containsExactlyInAnyOrderElementsOf(rest);
        assertThat(findPermanent(player1, chosen.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithFewerThanSevenCardsAndNoCreature() {
        Card first = new TurntimberSymbiosis();
        Card second = new TurntimberSymbiosis();
        setLibrary(first, second);
        castSymbiosis();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        setLibrary();
        castSymbiosis();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void landFaceEntersTappedWithoutOfferingUnaffordableLifePayment() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new TurntimberSymbiosis()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castSymbiosis() {
        harness.setHand(player1, List.of(new TurntimberSymbiosis()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
