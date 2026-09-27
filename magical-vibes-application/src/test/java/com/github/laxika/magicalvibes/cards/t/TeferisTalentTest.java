package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarnLivingLegacy;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AllowLoyaltyActivationAtInstantSpeedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisTalent.class, KothOfTheHammer.class, KarnLivingLegacy.class, GrizzlyBears.class})
class TeferisTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant only a planeswalker")
    void canEnchantOnlyPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TeferisTalent()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("planeswalker");
    }

    @Test
    @DisplayName("Whenever you draw a card, puts a loyalty counter on the enchanted planeswalker")
    void drawingPutsLoyaltyCounterOnEnchantedPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player1, new KothOfTheHammer(), 3);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(planeswalker.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ultimate creates an emblem that permits instant-speed loyalty activations")
    void ultimateCreatesInstantSpeedLoyaltyEmblem() {
        Permanent koth = addPlaneswalker(player1, new KothOfTheHammer(), 12);
        Permanent karn = addPlaneswalker(player1, new KarnLivingLegacy(), 1);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(koth.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.staticEffects()).containsExactly(new AllowLoyaltyActivationAtInstantSpeedEffect());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int karnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(karn);
        harness.activateAbility(player1, karnIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private Permanent addPlaneswalker(Player player, com.github.laxika.magicalvibes.model.Card card, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
