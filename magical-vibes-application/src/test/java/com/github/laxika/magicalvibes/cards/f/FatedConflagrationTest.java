package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SatyrWayfinder;
import com.github.laxika.magicalvibes.cards.k.KioraTheCrashingWave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatedConflagration.class, SatyrWayfinder.class, KioraTheCrashingWave.class})
class FatedConflagrationTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a target creature")
    void dealsFiveDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        prepareCast(player1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deals 5 damage to a target planeswalker")
    void dealsFiveDamageToPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2, 5);
        prepareCast(player1);

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("Scries 2 when cast during your turn")
    void scriesOnYourTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        harness.setLibrary(player1, List.of(new SatyrWayfinder(), new SatyrWayfinder(), new SatyrWayfinder()));
        prepareCast(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not scry during an opponent's turn")
    void doesNotScryOnOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        harness.setLibrary(player1, List.of(new SatyrWayfinder(), new SatyrWayfinder(), new SatyrWayfinder()));
        prepareCast(player2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareCast(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not scry when the only target leaves before resolution")
    void doesNotScryWithIllegalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        Card first = new SatyrWayfinder();
        Card second = new SatyrWayfinder();
        harness.setLibrary(player1, List.of(first, second));
        prepareCast(player1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FatedConflagration);
    }

    @Test
    @DisplayName("Scry can move one card to the bottom and keep one on top")
    void scryReordersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        Card first = new SatyrWayfinder();
        Card second = new KioraTheCrashingWave();
        Card third = new FatedConflagration();
        harness.setLibrary(player1, List.of(first, second, third));
        prepareCast(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can damage a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SatyrWayfinder());
        prepareCast(player2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Removes exactly five loyalty from a surviving planeswalker")
    void removesFiveLoyalty() {
        Permanent target = addPlaneswalker(player2, 7);
        prepareCast(player2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Scry handles a library with only one card")
    void scriesWithOneCardLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrWayfinder());
        Card onlyCard = new SatyrWayfinder();
        harness.setLibrary(player1, List.of(onlyCard));
        prepareCast(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareCast(Player activePlayer) {
        harness.setHand(player1, List.of(new FatedConflagration()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new KioraTheCrashingWave());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
