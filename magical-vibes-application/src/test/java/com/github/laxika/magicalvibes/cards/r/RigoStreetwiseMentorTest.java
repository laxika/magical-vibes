package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElspethResplendent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RigoStreetwiseMentor.class, Forest.class, GrizzlyBears.class, Ornithopter.class,
        ElspethResplendent.class, InvasionOfZendikar.class, Murder.class, Strangle.class})
class RigoStreetwiseMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent rigo = castRigo();

        assertThat(rigo.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws when one or more creatures with power 1 or less attack")
    void drawsForQualifyingAttack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws only one card for multiple qualifying attackers")
    void drawsOnlyOnceForMultipleQualifyingAttackers() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new Ornithopter());
        addCreatureReady(player1, new Ornithopter());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when no attacking creature has power 1 or less")
    void doesNotDrawForNonQualifyingAttack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws separately for a player and a planeswalker attacked by small creatures")
    void drawsForEachAttackedPlayerOrPlaneswalker() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new Ornithopter());
        addCreatureReady(player1, new Ornithopter());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethResplendent());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        declareAttackersAtTargets(List.of(1, 2), Map.of(1, player2.getId(), 2, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws for a qualifying attack on only a planeswalker")
    void drawsForPlaneswalkerAttack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new Ornithopter());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethResplendent());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        declareAttackersAtTargets(List.of(1), Map.of(1, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw for attacking only a battle")
    void doesNotDrawForBattleAttack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player1, new Ornithopter());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());

        declareAttackersAtTargets(List.of(1), Map.of(1, battle.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Uses power at declaration and still draws if the attacker grows before resolution")
    void powerIsCheckedWhenAttackersAreDeclared() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        Permanent attacker = addCreatureReady(player1, new Ornithopter());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw if counters raise an attacker's power above one")
    void doesNotDrawForBoostedSmallCreature() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        Permanent attacker = addCreatureReady(player1, new Ornithopter());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an opponent attacks with a small creature")
    void doesNotDrawForOpponentAttack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new RigoStreetwiseMentor());
        addCreatureReady(player2, new Ornithopter());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The shield prevents lethal damage once, then a second damage spell kills Rigo")
    void shieldPreventsDamageOnce() {
        Permanent rigo = castRigo();
        harness.setHand(player1, List.of(new Strangle(), new Strangle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, rigo.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rigo);
        assertThat(rigo.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(rigo.getMarkedDamage()).isZero();

        harness.castAndResolveSorcery(player1, 0, rigo.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rigo);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rigo.getCard());
    }

    @Test
    @DisplayName("The shield replaces destruction once, then a second destroy spell kills Rigo")
    void shieldPreventsDestructionOnce() {
        Permanent rigo = castRigo();
        harness.setHand(player1, List.of(new Murder(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player1, 0, rigo.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rigo);
        assertThat(rigo.getCounterCount(CounterType.SHIELD)).isZero();

        harness.castAndResolveInstant(player1, 0, rigo.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rigo);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rigo.getCard());
    }

    private void declareAttackersAtTargets(List<Integer> attackerIndices, Map<Integer, UUID> targets) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices, targets);
    }

    private Permanent castRigo() {
        harness.setHand(player1, List.of(new RigoStreetwiseMentor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Rigo, Streetwise Mentor");
    }
}
