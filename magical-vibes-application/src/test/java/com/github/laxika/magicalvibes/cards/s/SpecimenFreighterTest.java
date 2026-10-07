package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudsculptTechnician;
import com.github.laxika.magicalvibes.cards.l.LumenClassFrigate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SpecimenFreighter.class, CloudsculptTechnician.class, LumenClassFrigate.class})
class SpecimenFreighterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to two non-Spacecraft creatures to their owners' hands")
    void etbReturnsTwoNonSpacecraftCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CloudsculptTechnician());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CloudsculptTechnician());

        castFreighter(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(ownCreature.getCard().getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(opposingCreature.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature, opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("ETB cannot target an animated Spacecraft creature")
    void etbCannotTargetSpacecraftCreature() {
        Permanent spacecraft = harness.addToBattlefieldAndReturn(player2, new LumenClassFrigate());
        spacecraft.setCounterCount(CounterType.CHARGE, 12);

        harness.setHand(player1, List.of(new SpecimenFreighter()));
        addFreighterMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(spacecraft.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Spacecraft creature");
    }

    @Test
    @DisplayName("Station uses the tapped creature's power and unlocks flying at nine charge counters")
    void stationUsesCreaturePowerAndUnlocksFlying() {
        Permanent freighter = harness.addToBattlefieldAndReturn(player1, new SpecimenFreighter());
        Permanent technician = addCreatureReady(player1, new CloudsculptTechnician());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(freighter), null, null);
        technician.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(technician.isTapped()).isTrue();
        assertThat(freighter.getCounterCount(CounterType.CHARGE)).isEqualTo(3);

        freighter.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking mills four cards from the defending player's library")
    void attackingMillsDefendingPlayer() {
        Permanent freighter = addCreatureReady(player1, new SpecimenFreighter());
        freighter.setCounterCount(CounterType.CHARGE, 9);
        List<Card> cards = List.of(new CloudsculptTechnician(), new CloudsculptTechnician(),
                new CloudsculptTechnician(), new CloudsculptTechnician());
        harness.setLibrary(player2, cards);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(cards);
    }

    @Test
    void etbCanChooseNoTargetsWhenCreaturesAreAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CloudsculptTechnician());

        castFreighter(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .anyMatch(SpecimenFreighter.class::isInstance);
    }

    @Test
    void etbCanReturnJustOneCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CloudsculptTechnician());

        castFreighter(List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void stationCanTapSummoningSickCreatureAndAccumulatesCounters() {
        Permanent freighter = harness.addToBattlefieldAndReturn(player1, new SpecimenFreighter());
        Permanent technician = harness.addToBattlefieldAndReturn(player1, new CloudsculptTechnician());
        technician.setSummoningSick(true);
        freighter.setCounterCount(CounterType.CHARGE, 7);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(technician.isTapped()).isTrue();
        assertThat(freighter.getCounterCount(CounterType.CHARGE)).isEqualTo(9);
        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FLYING)).isTrue();
    }

    @Test
    void stationCannotTapTheFreighterItself() {
        Permanent freighter = addCreatureReady(player1, new SpecimenFreighter());
        freighter.setCounterCount(CounterType.CHARGE, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(freighter.isTapped()).isFalse();
        assertThat(freighter.getCounterCount(CounterType.CHARGE)).isEqualTo(9);
    }

    @Test
    void stationCannotTapAnAlreadyTappedCreature() {
        Permanent freighter = harness.addToBattlefieldAndReturn(player1, new SpecimenFreighter());
        Permanent technician = harness.addToBattlefieldAndReturn(player1, new CloudsculptTechnician());
        technician.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(freighter.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new SpecimenFreighter());
        Permanent technician = harness.addToBattlefieldAndReturn(player1, new CloudsculptTechnician());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(technician.isTapped()).isFalse();
    }

    @Test
    void etbCannotTargetMoreThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CloudsculptTechnician());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CloudsculptTechnician());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new CloudsculptTechnician());
        harness.setHand(player1, List.of(new SpecimenFreighter()));
        addFreighterMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationCannotBeActivatedDuringOpponentsMainPhase() {
        harness.addToBattlefield(player1, new SpecimenFreighter());
        Permanent technician = harness.addToBattlefieldAndReturn(player1, new CloudsculptTechnician());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(technician.isTapped()).isFalse();
    }

    @Test
    void attackingMillsAllRemainingCardsWhenLibraryHasFewerThanFour() {
        Permanent freighter = addCreatureReady(player1, new SpecimenFreighter());
        freighter.setCounterCount(CounterType.CHARGE, 9);
        List<Card> cards = List.of(new CloudsculptTechnician(), new CloudsculptTechnician());
        harness.setLibrary(player2, cards);
        List<Card> attackingPlayersLibrary = List.of(new CloudsculptTechnician());
        harness.setLibrary(player1, attackingPlayersLibrary);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(attackingPlayersLibrary);
    }

    @Test
    void losesCreatureTypeAndFlyingBelowNineCounters() {
        Permanent freighter = harness.addToBattlefieldAndReturn(player1, new SpecimenFreighter());
        freighter.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, freighter)).isFalse();
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FLYING)).isFalse();

        freighter.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, freighter)).isTrue();
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FLYING)).isTrue();

        freighter.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, freighter)).isFalse();
        assertThat(gqs.hasKeyword(gd, freighter, Keyword.FLYING)).isFalse();
    }

    private void castFreighter(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SpecimenFreighter()));
        addFreighterMana();
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }

    private void addFreighterMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
