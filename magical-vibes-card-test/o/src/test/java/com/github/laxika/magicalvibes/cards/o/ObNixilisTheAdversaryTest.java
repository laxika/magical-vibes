package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CleverImpersonator;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LordXanderTheCollector;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.r.RaffinesInformant;
import com.github.laxika.magicalvibes.cards.s.SuspiciousBookcase;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObNixilisTheAdversary.class, Murder.class, Forest.class, RaffinesInformant.class,
        LordXanderTheCollector.class, CleverImpersonator.class, SuspiciousBookcase.class})
class ObNixilisTheAdversaryTest extends BaseCardTest {

    @Test
    @DisplayName("Casualty X copies Ob Nixilis as a nonlegendary token with X loyalty")
    void casualtyCopiesWithChosenLoyalty() {
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new RaffinesInformant());
        harness.setHand(player1, List.of(new ObNixilisTheAdversary()));
        addObMana();

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false,
                casualtyCreature.getId());
        resolveStackEntries(4);

        List<Permanent> obNixilis = findPermanents(player1, "Ob Nixilis, the Adversary");
        assertThat(obNixilis).hasSize(2);
        assertThat(obNixilis).anySatisfy(permanent ->
                assertThat(permanent.getCounterCount(CounterType.LOYALTY)).isEqualTo(3));
        assertThat(obNixilis).anySatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isTrue();
            assertThat(permanent.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Casualty X rejects a creature with power below the chosen X")
    void casualtyRequiresSufficientPower() {
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new RaffinesInformant());
        harness.setHand(player1, List.of(new ObNixilisTheAdversary()));
        addObMana();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false,
                casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(casualtyCreature);
    }

    @Test
    @DisplayName("Plus one makes opponents discard or lose life and gains life with a Demon")
    void plusOne() {
        Permanent obNixilis = addReadyOb(player1, 3);
        harness.addToBattlefield(player1, new LordXanderTheCollector());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(obNixilis.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Minus two creates a Devil whose death trigger deals damage")
    void minusTwoCreatesDevil() {
        addReadyOb(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent devil = findPermanents(player1, "Devil").getFirst();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, devil.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Minus seven makes a target player draw seven and lose seven life")
    void minusSeven() {
        addReadyOb(player1, 7);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Casualty accepts a creature whose power exceeds the chosen X")
    void casualtyAcceptsGreaterPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaffinesInformant());
        harness.setHand(player1, List.of(new ObNixilisTheAdversary()));
        addObMana();

        gs.playCard(gd, player1, 0, 1, null, null, List.of(), List.of(), false,
                creature.getId());
        resolveStackEntries(3);

        assertThat(findPermanents(player1, "Ob Nixilis, the Adversary")).hasSize(2)
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
                });
        harness.assertInGraveyard(player1, "Raffine's Informant");
    }

    @Test
    @DisplayName("Casting without casualty creates only the original planeswalker")
    void mayCastWithoutCasualty() {
        harness.setHand(player1, List.of(new ObNixilisTheAdversary()));
        addObMana();

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        List<Permanent> permanents = findPermanents(player1, "Ob Nixilis, the Adversary");
        assertThat(permanents).hasSize(1);
        assertThat(permanents.getFirst().getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(permanents.getFirst().getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("Discarding avoids life loss and a Devil still grants life")
    void plusOneWithDiscardAndDevil() {
        addReadyOb(player1, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.performUntapStep(player1);
        Forest discarded = new Forest();
        harness.setHand(player2, List.of(discarded));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("An opponent can keep a card and lose life; no Demon or Devil means no gain")
    void plusOneMayDeclineDiscard() {
        addReadyOb(player1, 3);
        Forest kept = new Forest();
        harness.setHand(player2, List.of(kept));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("The final ability can target its controller and resolves after loyalty is spent")
    void minusSevenMayTargetController() {
        addReadyOb(player1, 7);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertLife(player1, 13);
        harness.assertNotOnBattlefield(player1, "Ob Nixilis, the Adversary");
        harness.assertInGraveyard(player1, "Ob Nixilis, the Adversary");
    }

    @Test
    @DisplayName("Copying a casualty token preserves its chosen starting loyalty")
    void furtherCopyPreservesCasualtyStartingLoyalty() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaffinesInformant());
        harness.setHand(player1, List.of(new ObNixilisTheAdversary()));
        addObMana();
        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(), false,
                creature.getId());
        resolveStackEntries(3);
        Permanent token = findPermanents(player1, "Ob Nixilis, the Adversary").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        token.setCounterCount(CounterType.LOYALTY, 5);

        harness.castFromHand(player1, new CleverImpersonator(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof CleverImpersonator)
                .findFirst().orElseThrow();
        assertThat(copy.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(token.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(findPermanents(player1, "Ob Nixilis, the Adversary")).hasSize(3);
    }

    @Test
    @DisplayName("A casualty copy with X zero dies instead of gaining printed loyalty")
    void zeroLoyaltyCasualtyCopyDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SuspiciousBookcase());
        harness.setHand(player1, List.of(new ObNixilisTheAdversary()));
        addObMana();

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                creature.getId());
        resolveStackEntries(3);

        List<Permanent> permanents = findPermanents(player1, "Ob Nixilis, the Adversary");
        assertThat(permanents).hasSize(1);
        assertThat(permanents.getFirst().getCard().isToken()).isFalse();
        assertThat(permanents.getFirst().getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Suspicious Bookcase");
    }

    private void addObMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReadyOb(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ObNixilisTheAdversary());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void resolveStackEntries(int count) {
        for (int i = 0; i < count; i++) {
            harness.passBothPriorities();
        }
    }
}
