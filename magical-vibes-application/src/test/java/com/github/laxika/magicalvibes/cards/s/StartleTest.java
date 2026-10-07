package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({Startle.class, DawnhartRejuvenator.class, Island.class})
class StartleTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature -2/-0, creates a decayed Zombie, and draws a card")
    void resolvesAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        castStartle(target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(0);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
        assertThat(bls.canBlock(gd, zombie)).isFalse();
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void powerReductionWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        castStartle(target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Startle()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An illegal sole target prevents both token creation and drawing")
    void illegalTargetPreventsAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Startle()));
        addMana();
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Startle");
    }

    @Test
    @DisplayName("Can target your own creature and stack the power reductions below zero")
    void canReduceOwnCreatureToNegativePower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        castStartle(target.getId());
        castStartle(target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The decayed Zombie deals damage and is sacrificed at end of combat")
    void attackingZombieIsSacrificed() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        castStartle(target.getId());
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zombie)));
            resolveCombat();
            harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        });

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    @Test
    @DisplayName("A decayed Zombie that does not attack survives combat")
    void nonattackingZombieSurvivesCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        castStartle(target.getId());

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            declareAttackers(List.of());
            harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        });

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Decayed puts its delayed sacrifice on the stack before the Zombie dies")
    void delayedSacrificeAllowsResponses() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        castStartle(target.getId());
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zombie)));
            resolveCombat();
            harness.passUntil(TurnStep.END_OF_COMBAT);

            assertThat(gd.playerBattlefields.get(player1.getId())).contains(zombie);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
        });

        harness.assertNotOnBattlefield(player1, "Zombie");
    }

    private void castStartle(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Startle()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
