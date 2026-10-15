package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.cards.d.DigUpTheBody;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalcoSparaPactweaver.class, CivicGardener.class, DigUpTheBody.class})
class FalcoSparaPactweaverTest extends BaseCardTest {

    @Test
    void entersWithShieldCounter() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());

        assertThat(falco.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void castsSpellFromLibraryTopByRemovingCounterFromCreature() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        Card spell = new CivicGardener();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1, List.of(falco.getId()));

        harness.assertOnBattlefield(player1, "Civic Gardener");
        assertThat(falco.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void requiresCounterPaymentForSpellFromLibraryTop() {
        harness.addToBattlefield(player1, new FalcoSparaPactweaver());
        Card spell = new CivicGardener();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);
    }
    @Test
    void looksAtTopCardPrivatelyWithoutCountersDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new FalcoSparaPactweaver());
        harness.setLibrary(player1, List.of(new CivicGardener()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Civic Gardener") && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void canRemoveCounterFromAnotherControlledCreature() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new CivicGardener());
        gardener.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new CivicGardener()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1, List.of(gardener.getId()));

        assertThat(gardener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(falco.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(countPermanents(player1, "Civic Gardener")).isEqualTo(2);
    }

    @Test
    void cannotRemoveCounterFromOpponentsCreature() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        Permanent gardener = harness.addToBattlefieldAndReturn(player2, new CivicGardener());
        gardener.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card spell = new CivicGardener();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, List.of(gardener.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gardener.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(falco.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastWithoutPayingManaAndDoesNotConsumeCounter() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        Card spell = new CivicGardener();
        harness.setLibrary(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, List.of(falco.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(falco.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantCreatureSpellsInstantTiming() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        Card spell = new CivicGardener();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, List.of(falco.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(falco.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);
    }

    @Test
    void castsCasualtySpellWithoutPayingOptionalCasualtyCost() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        Card spell = new DigUpTheBody();
        harness.setLibrary(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromLibraryTop(player1, List.of(falco.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(falco.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void canCastMoreThanOneSpellFromLibraryInTheSameTurn() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        falco.setCounterCount(CounterType.SHIELD, 2);
        harness.setLibrary(player1, List.of(new CivicGardener(), new CivicGardener()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveFromLibraryTop(player1, List.of(falco.getId()));
        harness.castAndResolveFromLibraryTop(player1, List.of(falco.getId()));

        assertThat(countPermanents(player1, "Civic Gardener")).isEqualTo(2);
        assertThat(falco.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void castingFromHandDoesNotRequireRemovingCounter() {
        Permanent falco = harness.enterBattlefieldAndReturn(player1, new FalcoSparaPactweaver());
        harness.setHand(player1, List.of(new CivicGardener()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Civic Gardener");
        assertThat(falco.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }
}
