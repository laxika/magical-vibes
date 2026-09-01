package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GuerrillaTactics;
import com.github.laxika.magicalvibes.cards.i.IvoryGargoyle;
import com.github.laxika.magicalvibes.cards.k.KjeldoranEscort;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ScarsOfTheVeteran.class,
        IvoryGargoyle.class,
        GuerrillaTactics.class,
        KjeldoranEscort.class,
        AjaniGoldmane.class
})
class ScarsOfTheVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving on a creature adds a 7-damage prevention-to-counters shield")
    void resolvingOnCreatureAddsShield() {
        harness.addToBattlefield(player1, new IvoryGargoyle());
        harness.setHand(player1, List.of(new ScarsOfTheVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Ivory Gargoyle");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gargoyle(player1).getDamageToCounterPreventionShield()).isEqualTo(7);
    }

    @Test
    @DisplayName("Resolving on a player adds a 7-damage player prevention shield")
    void resolvingOnPlayerAddsShield() {
        harness.setHand(player1, List.of(new ScarsOfTheVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(7);
    }

    @Test
    @DisplayName("Prevented damage to a player does not reduce life")
    void preventedDamageToPlayerDoesNotReduceLife() {
        int startingLife = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new ScarsOfTheVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GuerrillaTactics()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isEqualTo(5);
    }

    @Test
    @DisplayName("Prevented damage to a creature becomes +0/+1 counters at the next end step")
    void preventedDamageBecomesCountersAtEndStep() {
        harness.addToBattlefield(player1, new IvoryGargoyle());
        harness.setHand(player1, List.of(new ScarsOfTheVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player1, "Ivory Gargoyle");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GuerrillaTactics()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        Permanent gargoyle = gargoyle(player1);
        assertThat(gargoyle.getDamageToCounterPreventionShield()).isEqualTo(5);
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();

        advanceToEndStep(player1);
        resolveAllTriggers();

        Permanent afterEnd = gargoyle(player1);
        assertThat(afterEnd.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterEnd)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterEnd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targeting a planeswalker does not create creature counters")
    void targetingPlaneswalkerDoesNotCreateCreatureCounters() {
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new ScarsOfTheVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, ajani.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GuerrillaTactics()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, ajani.getId());
        harness.passBothPriorities();

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(ajani.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can be cast by exiling a white card from hand instead of paying mana")
    void castWithExileWhiteAlternateCost() {
        harness.addToBattlefield(player1, new IvoryGargoyle());
        harness.setHand(player1, List.of(new ScarsOfTheVeteran(), new KjeldoranEscort()));

        UUID targetId = harness.getPermanentId(player1, "Ivory Gargoyle");
        harness.castInstantWithAlternateExileFromHand(player1, 0, targetId, 1);
        harness.passBothPriorities();

        assertThat(gargoyle(player1).getDamageToCounterPreventionShield()).isEqualTo(7);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.exiledCards.getFirst().card()).isInstanceOf(KjeldoranEscort.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects exiling a non-white card")
    void alternateCostRequiresWhiteCard() {
        harness.addToBattlefield(player1, new IvoryGargoyle());
        harness.setHand(player1, List.of(new ScarsOfTheVeteran(), new GuerrillaTactics()));

        UUID targetId = harness.getPermanentId(player1, "Ivory Gargoyle");
        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, targetId, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting puts Scars of the Veteran on the stack targeting the chosen creature")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player2, new IvoryGargoyle());
        harness.setHand(player1, List.of(new ScarsOfTheVeteran()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Ivory Gargoyle");
        harness.castInstant(player1, 0, targetId);

        GameData game = harness.getGameData();
        assertThat(game.stack).hasSize(1);
        StackEntry entry = game.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    private Permanent gargoyle(Player player) {
        return findPermanent(player, "Ivory Gargoyle");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
