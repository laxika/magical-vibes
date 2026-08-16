package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrzaLordProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces artifact spells by {1}")
    void reducesArtifactSpells() {
        harness.addToBattlefield(player1, new UrzaLordProtector());
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce creature spells")
    void doesNotReduceCreatureSpells() {
        harness.addToBattlefield(player1, new UrzaLordProtector());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Melds with The Mightstone and Weakstone")
    void meldsWithMightstoneAndWeakstone() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaLordProtector());
        urza.setSummoningSick(false);
        harness.addToBattlefield(player1, namedArtifact("The Mightstone and Weakstone"));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urza, Lord Protector");
        harness.assertNotOnBattlefield(player1, "The Mightstone and Weakstone");
        harness.assertOnBattlefield(player1, "Urza, Planeswalker");
        assertThat(findPermanent(player1, "Urza, Planeswalker").getMeldComponentCards())
                .hasSize(2);
    }

    @Test
    @DisplayName("Urza's plus-two ability reduces matching spells and gains life")
    void plusTwoReducesMatchingSpellsAndGainsLife() {
        addReadyUrza(7);
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Urza's plus-one ability draws two cards, then discards one")
    void plusOneDrawsAndDiscards() {
        addReadyUrza(7);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Urza's zero ability creates two artifact Soldiers")
    void zeroCreatesArtifactSoldiers() {
        addReadyUrza(7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(findPermanents(player1, "Soldier"))
                .allMatch(permanent -> permanent.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("Urza's minus-three ability exiles a nonland permanent")
    void minusThreeExilesNonlandPermanent() {
        addReadyUrza(7);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();

        harness.activateAbility(player1, 0, 3, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Urza's minus-ten ability protects artifacts and planeswalkers")
    void minusTenProtectsArtifactsAndPlaneswalkers() {
        addReadyUrza(10);
        Permanent garruk = harness.addToBattlefieldAndReturn(player1, new GarrukWildspeaker());
        garruk.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 4, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Urza, Planeswalker");
        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");
        harness.assertOnBattlefield(player1, "Jhoira's Familiar");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Urza can activate loyalty abilities twice each turn")
    void canActivateLoyaltyAbilitiesTwice() {
        Permanent urza = addReadyUrza(7);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(urza.getCounterCount(CounterType.LOYALTY)).isEqualTo(11);
    }

    private Permanent addReadyUrza(int loyalty) {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPlaneswalker());
        urza.setCounterCount(CounterType.LOYALTY, loyalty);
        urza.setSummoningSick(false);
        prepareMainPhase();
        return urza;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private static Card namedArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }
}
