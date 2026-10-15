package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mycologist.class, ArtificialEvolution.class, Bitterblossom.class})
class MycologistTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent mycologist = addMycologist();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(mycologist.getCounterCount(CounterType.FUNGUS)).isOne();
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent mycologist = addMycologist();
        mycologist.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mycologist.getCounterCount(CounterType.FUNGUS)).isZero();
        Permanent saproling = findPermanent(player1, "Saproling");
        assertThat(saproling.getCard().isToken()).isTrue();
        assertThat(saproling.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(gqs.getEffectivePower(gd, saproling)).isOne();
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isOne();
    }

    @Test
    @DisplayName("Removing three spore counters leaves additional counters")
    void removesExactlyThreeSporeCounters() {
        Permanent mycologist = addMycologist();
        mycologist.setCounterCount(CounterType.FUNGUS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mycologist.getCounterCount(CounterType.FUNGUS)).isOne();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addMycologist().setCounterCount(CounterType.FUNGUS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a Saproling gains 2 life")
    void sacrificingSaprolingGainsLife() {
        Permanent mycologist = addMycologist();
        mycologist.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("The life-gain ability requires a Saproling")
    void lifeGainAbilityRequiresSaproling() {
        addMycologist();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's upkeep does not add a spore counter")
    void opponentUpkeepDoesNotAddSporeCounter() {
        Permanent mycologist = addMycologist();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(mycologist.getCounterCount(CounterType.FUNGUS)).isZero();
    }

    @Test
    @DisplayName("Spore counters are paid before the Saproling is created")
    void sporeCountersAreAnActivationCost() {
        Permanent mycologist = addMycologist();
        mycologist.setCounterCount(CounterType.FUNGUS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mycologist.getCounterCount(CounterType.FUNGUS)).isZero();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Both abilities work while Mycologist is tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent mycologist = harness.addToBattlefieldAndReturn(player1, new Mycologist());
        mycologist.setSummoningSick(true);
        mycologist.tap();
        mycologist.setCounterCount(CounterType.FUNGUS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("An opponent's Saproling cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsSaproling() {
        Permanent opponentMycologist = addCreatureReady(player2, new Mycologist());
        opponentMycologist.setCounterCount(CounterType.FUNGUS, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        addMycologist();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("A noncreature kindred Saproling can pay the sacrifice cost")
    void canSacrificeNoncreatureKindredSaproling() {
        addMycologist();
        Permanent bitterblossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, bitterblossom.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SAPROLING");
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bitterblossom);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bitterblossom.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    private Permanent addMycologist() {
        return addCreatureReady(player1, new Mycologist());
    }

}
