package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dinotomaton.class, ArmoredKincaller.class, Abrade.class})
class DinotomatonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a creature you control menace until end of turn")
    void etbGrantsMenaceToControlledCreature() {
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        castDinotomaton();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("ETB menace remains through the end step and wears off during cleanup")
    void etbMenaceWearsOffAtEndOfTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        castDinotomaton();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target a creature an opponent controls")
    void etbCannotTargetOpponentCreature() {
        Permanent opponent = addCreatureReady(player2, new ArmoredKincaller());
        castDinotomaton();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Dinotomaton").getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Dinotomaton can target itself when it is the only creature you control")
    void canTargetItselfOnOtherwiseEmptyBattlefield() {
        castDinotomaton();
        Permanent source = findPermanent(player1, "Dinotomaton");
        harness.handlePermanentChosen(player1, source.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passUntil(player2, TurnStep.UPKEEP);
        source.setSummoningSick(false);
        addCreatureReady(player2, new ArmoredKincaller());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("The ETB grants menace even if Dinotomaton leaves before it resolves")
    void triggerResolvesAfterSourceIsDestroyed() {
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        castDinotomaton();
        Permanent source = findPermanent(player1, "Dinotomaton");
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, source.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The ETB does not grant menace to another creature when its target is destroyed")
    void removedTargetDoesNotRedirectTheGrant() {
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        Permanent other = addCreatureReady(player1, new ArmoredKincaller());
        castDinotomaton();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gqs.hasKeyword(gd, other, Keyword.MENACE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature granted menace can be blocked by two creatures")
    void grantedMenaceAllowsTwoBlockers() {
        Permanent target = addCreatureReady(player1, new ArmoredKincaller());
        Permanent firstBlocker = addCreatureReady(player2, new ArmoredKincaller());
        Permanent secondBlocker = addCreatureReady(player2, new ArmoredKincaller());
        castDinotomaton();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    private void castDinotomaton() {
        harness.setHand(player1, List.of(new Dinotomaton()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
