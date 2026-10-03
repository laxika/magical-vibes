package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CallTheScions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathlessBehemoth.class, CallTheScions.class})
class DeathlessBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard to hand by sacrificing two Eldrazi Scions")
    void returnsFromGraveyardBySacrificingTwoEldraziScions() {
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        Permanent firstScion = harness.addToBattlefieldAndReturn(player1, createScion());
        Permanent secondScion = harness.addToBattlefieldAndReturn(player1, createScion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deathless Behemoth");
        harness.assertInGraveyard(player1, "Eldrazi Scion");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstScion, secondScion);
    }

    @Test
    @DisplayName("Does not accept Eldrazi Spawn for the sacrifice cost")
    void doesNotAcceptEldraziSpawnForTheSacrificeCost() {
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        harness.addToBattlefield(player1, createScion());
        harness.addToBattlefield(player1, createSpawn());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void canOnlyBeActivatedAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        harness.addToBattlefield(player1, createScion());
        harness.addToBattlefield(player1, createScion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Sacrificing real tapped Scion tokens pays the cost before the return resolves")
    void paysSacrificeCostBeforeResolution() {
        createRealScions();
        List<Permanent> scions = findPermanents(player1, "Eldrazi Scion");
        scions.forEach(Permanent::tap);
        DeathlessBehemoth behemoth = new DeathlessBehemoth();
        harness.setGraveyard(player1, List.of(behemoth));

        harness.activateGraveyardAbility(player1, 0);

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(behemoth);
        harness.assertNotInHand(player1, "Deathless Behemoth");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(behemoth);
        harness.assertNotInGraveyard(player1, "Deathless Behemoth");
        harness.assertNotInGraveyard(player1, "Eldrazi Scion");
    }

    @Test
    @DisplayName("Only the activated copy returns when several Behemoths are in the graveyard")
    void returnsOnlyTheActivatedCopy() {
        createRealScions();
        DeathlessBehemoth other = new DeathlessBehemoth();
        DeathlessBehemoth activated = new DeathlessBehemoth();
        harness.setGraveyard(player1, List.of(other, activated));

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(activated).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Opponent-controlled Scions cannot pay the cost")
    void cannotSacrificeOpponentsScions() {
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        harness.addToBattlefield(player1, createScion());
        harness.addToBattlefield(player2, createScion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents");

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Scion")).hasSize(1);
        harness.assertInGraveyard(player1, "Deathless Behemoth");
    }

    @Test
    @DisplayName("The ability cannot be activated outside a main phase")
    void cannotActivateOutsideMainPhase() {
        createRealScions();
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    @DisplayName("The ability cannot be activated with a spell on the stack")
    void cannotActivateWithNonemptyStack() {
        createRealScions();
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));
        harness.castFromHand(player1, new CallTheScions(), "{2}{G}");

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
        harness.assertInGraveyard(player1, "Deathless Behemoth");
    }

    @Test
    @DisplayName("The controller chooses exactly two Scions when more are available")
    void choosesTwoScionsFromFour() {
        createRealScions();
        createRealScions();
        List<Permanent> scions = findPermanents(player1, "Eldrazi Scion");
        harness.setGraveyard(player1, List.of(new DeathlessBehemoth()));

        harness.activateGraveyardAbility(player1, 0);
        harness.handlePermanentChosen(player1, scions.get(1).getId());
        harness.handlePermanentChosen(player1, scions.get(3).getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion"))
                .containsExactlyInAnyOrder(scions.get(0), scions.get(2));
        harness.assertInHand(player1, "Deathless Behemoth");
    }

    @Test
    @DisplayName("Vigilance allows Deathless Behemoth to attack without tapping")
    void attacksWithoutTapping() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new DeathlessBehemoth());
        behemoth.setSummoningSick(false);

        declareAttackers(List.of(0));

        assertThat(behemoth.isAttackedThisTurn()).isTrue();
        assertThat(behemoth.isTapped()).isFalse();
    }

    private void createRealScions() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CallTheScions(), "{2}{G}");
        harness.passBothPriorities();
    }

    private Card createScion() {
        Card card = new Card();
        card.setName("Eldrazi Scion");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.ELDRAZI, CardSubtype.SCION));
        return card;
    }

    private Card createSpawn() {
        Card card = createScion();
        card.setName("Eldrazi Spawn");
        card.setSubtypes(List.of(CardSubtype.ELDRAZI, CardSubtype.SPAWN));
        return card;
    }
}
