package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.cards.b.BondedHerdbeast;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.p.PortentTracker;
import com.github.laxika.magicalvibes.cards.w.WrennAndRealmbreaker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhaltaAndMavren.class, AlabasterHostSanctifier.class, BondedHerdbeast.class,
        InvasionOfZendikar.class, PortentTracker.class, WrennAndRealmbreaker.class})
class GhaltaAndMavrenTest extends BaseCardTest {

    @Test
    @DisplayName("Dinosaur mode uses the greatest power among other attackers")
    void dinosaurModeUsesGreatestOtherAttackerPower() {
        addCreatureReady(player1, new GhaltaAndMavren());
        addCreatureReady(player1, creature("Large attacker", 5, 5));
        addCreatureReady(player1, creature("Small attacker", 2, 2));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create a tapped and attacking X/X green Dinosaur creature token with trample.");

        Permanent dinosaur = findPermanent(player1, "Dinosaur");
        assertThat(dinosaur.getCard().getPower()).isEqualTo(5);
        assertThat(dinosaur.getCard().getToughness()).isEqualTo(5);
        assertThat(dinosaur.getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(dinosaur.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(dinosaur.isTapped()).isTrue();
        assertThat(dinosaur.isAttacking()).isTrue();
        assertThat(dinosaur.isAttackedThisTurn()).isFalse();
        assertThat(countPermanents(player1, "Vampire")).isZero();
    }

    @Test
    void modeIsChosenBeforePlayersCanRespondToAttackTrigger() {
        addCreatureReady(player1, new GhaltaAndMavren());
        addCreatureReady(player1, new AlabasterHostSanctifier());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1,
                "Create X 1/1 white Vampire creature tokens with lifelink.");
        assertThat(countPermanents(player1, "Vampire")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Vampire")).isEqualTo(1);
    }

    @Test
    void vampireModeExcludesSourceAndCreatesUntappedNonattackingTokens() {
        addCreatureReady(player1, new GhaltaAndMavren());
        addCreatureReady(player1, new AlabasterHostSanctifier());
        addCreatureReady(player1, new PortentTracker());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create X 1/1 white Vampire creature tokens with lifelink.");

        assertThat(findPermanents(player1, "Vampire")).hasSize(2).allSatisfy(vampire -> {
            assertThat(vampire.isTapped()).isFalse();
            assertThat(vampire.isAttacking()).isFalse();
        });
    }

    @Test
    void sourceAttackingAloneCreatesNoVampires() {
        addCreatureReady(player1, new GhaltaAndMavren());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create X 1/1 white Vampire creature tokens with lifelink.");

        assertThat(countPermanents(player1, "Vampire")).isZero();
    }

    @Test
    void sourceAttackingAloneCreatesDinosaurThatDiesImmediately() {
        addCreatureReady(player1, new GhaltaAndMavren());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create a tapped and attacking X/X green Dinosaur creature token with trample.");

        assertThat(countPermanents(player1, "Dinosaur")).isZero();
        assertThat(gameLogContains("Dinosaur")).isTrue();
        harness.assertOnBattlefield(player1, "Ghalta and Mavren");
    }

    @Test
    void dinosaurUsesEffectivePowerAtResolutionAndIgnoresNonattackers() {
        addCreatureReady(player1, new GhaltaAndMavren());
        Permanent attacker = addCreatureReady(player1, new AlabasterHostSanctifier());
        addCreatureReady(player1, new BondedHerdbeast());

        declareAttackers(List.of(1));
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create a tapped and attacking X/X green Dinosaur creature token with trample.");

        Permanent dinosaur = findPermanent(player1, "Dinosaur");
        assertThat(dinosaur.getCard().getPower()).isEqualTo(3);
        assertThat(dinosaur.getCard().getToughness()).isEqualTo(3);
        assertThat(dinosaur.getCard().getColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    void vampireCountExcludesCreaturesThatHaveLeftCombatBeforeResolution() {
        addCreatureReady(player1, new GhaltaAndMavren());
        Permanent attacker = addCreatureReady(player1, new AlabasterHostSanctifier());
        addCreatureReady(player1, new PortentTracker());

        declareAttackers(List.of(1, 2));
        attacker.setAttacking(false);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create X 1/1 white Vampire creature tokens with lifelink.");

        assertThat(countPermanents(player1, "Vampire")).isEqualTo(1);
    }

    @Test
    void dinosaurCanAttackBattleProtectedByOpponent() {
        addCreatureReady(player1, new GhaltaAndMavren());
        addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create a tapped and attacking X/X green Dinosaur creature token with trample.");

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, battle.getId());
        assertThat(findPermanent(player1, "Dinosaur").getAttackTarget()).isEqualTo(battle.getId());
    }

    @Test
    void dinosaurRetainsCalculatedSizeWhenChoosingAttackDestination() {
        addCreatureReady(player1, new GhaltaAndMavren());
        addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new WrennAndRealmbreaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create a tapped and attacking X/X green Dinosaur creature token with trample.");
        harness.handlePermanentChosen(player1, planeswalker.getId());

        Permanent dinosaur = findPermanent(player1, "Dinosaur");
        assertThat(dinosaur.getCard().getPower()).isEqualTo(2);
        assertThat(dinosaur.getCard().getToughness()).isEqualTo(2);
        assertThat(dinosaur.getAttackTarget()).isEqualTo(planeswalker.getId());
        assertThat(dinosaur.isTapped()).isTrue();
        assertThat(dinosaur.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Vampire mode triggers when other creatures attack without Ghalta and Mavren")
    void vampireModeTriggersWithoutSourceAttacking() {
        addCreatureReady(player1, new GhaltaAndMavren());
        addCreatureReady(player1, creature("Attacker one", 3, 3));
        addCreatureReady(player1, creature("Attacker two", 2, 2));
        addCreatureReady(player1, creature("Attacker three", 1, 1));

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Create X 1/1 white Vampire creature tokens with lifelink.");

        List<Permanent> vampires = findPermanents(player1, "Vampire");
        assertThat(vampires).hasSize(3);
        assertThat(vampires).allSatisfy(vampire -> {
            assertThat(vampire.getCard().getPower()).isEqualTo(1);
            assertThat(vampire.getCard().getToughness()).isEqualTo(1);
            assertThat(vampire.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(vampire.getCard().getSubtypes()).containsExactly(CardSubtype.VAMPIRE);
            assertThat(vampire.getCard().getKeywords()).contains(Keyword.LIFELINK);
        });
        assertThat(countPermanents(player1, "Dinosaur")).isZero();
    }

    private Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of());
        return card;
    }
}
