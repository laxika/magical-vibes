package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheGreatWork;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Urabrask.class, TheGreatWork.class, DarkRitual.class, GrizzlyBears.class})
class UrabraskTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant or sorcery damages an opponent and adds red mana")
    void spellCastDamagesOpponentAndAddsMana() {
        harness.addToBattlefield(player1, new Urabrask());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Urabrask transforms after its controller casts three instant or sorcery spells")
    void transformsAfterThreeInstantOrSorcerySpells() {
        Permanent urabrask = addCreatureReady(player1, new Urabrask());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        for (int i = 0; i < 6; i++) {
            harness.passBothPriorities();
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(urabrask), null, null);
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "The Great Work");
        assertThat(saga).isNotNull();
        assertThat(saga.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The Great Work chapter I damages the opponent and their creature")
    void chapterIDamagesOpponentAndTheirCreature() {
        Permanent saga = addBackFaceSaga(0);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        advanceSagaToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Great Work chapter II creates three Treasures")
    void chapterIICreatesThreeTreasures() {
        Permanent saga = addBackFaceSaga(1);

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Great Work chapter III permits casting from any graveyard and returns Urabrask")
    void chapterIIICastsFromAnyGraveyardAndReturnsFrontFace() {
        Permanent saga = addBackFaceSaga(2);
        Card ritual = new DarkRitual();
        harness.setGraveyard(player2, List.of(ritual));

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Urabrask")).isNotNull();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, ritual.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(ritual);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(ritual);
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Urabrask")
    void opponentsInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new Urabrask());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Casting a creature does not trigger Urabrask")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Urabrask());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Two instant spells are insufficient to activate Urabrask")
    void cannotTransformAfterOnlyTwoSpells() {
        harness.addToBattlefield(player1, new Urabrask());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        for (int i = 0; i < 2; i++) {
            harness.castInstant(player1, 0);
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three");
        harness.assertOnBattlefield(player1, "Urabrask");
    }

    @Test
    @DisplayName("Chapter I leaves its controller's creatures unharmed")
    void chapterIDoesNotDamageOwnCreatures() {
        addBackFaceSaga(0);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        advanceSagaToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Chapter III allows multiple spells from its controller's graveyard")
    void chapterIIIAllowsMultipleOwnGraveyardSpells() {
        addBackFaceSaga(2);
        Card first = new DarkRitual();
        Card second = new DarkRitual();
        harness.setGraveyard(player1, List.of(first, second));
        advanceSagaToNextChapter();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);

        for (Card ritual : List.of(first, second)) {
            harness.castFromGraveyard(player1, ritual.getId());
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(first, second);
    }

    @Test
    @DisplayName("Chapter III does not permit casting creature cards from graveyards")
    void chapterIIIDoesNotAllowCreatureSpells() {
        addBackFaceSaga(2);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        advanceSagaToNextChapter();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Chapter III still requires paying a spell's mana cost")
    void chapterIIIDoesNotWaiveManaCosts() {
        addBackFaceSaga(2);
        Card ritual = new DarkRitual();
        harness.setGraveyard(player1, List.of(ritual));
        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, ritual.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ritual);
        harness.assertLife(player2, 20);
    }

    private Permanent addBackFaceSaga(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Urabrask());
        saga.setCard(saga.getOriginalCard().getBackFaceCard());
        saga.setTransformed(true);
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceSagaToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
