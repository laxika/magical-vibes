package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KeskitTheFleshSculptor;
import com.github.laxika.magicalvibes.cards.k.KarnLivingLegacy;
import com.github.laxika.magicalvibes.cards.e.ElspethSunsChampion;
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

@CardUsed({TeferisTalent.class, ElspethSunsChampion.class, KarnLivingLegacy.class, KeskitTheFleshSculptor.class})
class TeferisTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant only a planeswalker")
    void canEnchantOnlyPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KeskitTheFleshSculptor());
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
        Permanent planeswalker = addPlaneswalker(player1, new ElspethSunsChampion(), 3);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(planeswalker.getId());
        harness.setLibrary(player1, List.of(new KeskitTheFleshSculptor()));

        advanceToDraw(player1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ultimate creates an emblem that permits instant-speed loyalty activations")
    void ultimateCreatesInstantSpeedLoyaltyEmblem() {
        Permanent elspeth = addPlaneswalker(player1, new ElspethSunsChampion(), 12);
        Permanent karn = addPlaneswalker(player1, new KarnLivingLegacy(), 1);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(elspeth.getId());

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
        assertThatThrownBy(() -> harness.activateAbility(player1, karnIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only one loyalty ability");
    }

    @Test
    @DisplayName("Drawing twice adds two counters without choosing another planeswalker")
    void multipleDrawsAffectOnlyEnchantedPlaneswalker() {
        Permanent enchanted = addPlaneswalker(player1, new ElspethSunsChampion(), 3);
        Permanent other = addPlaneswalker(player1, new KarnLivingLegacy(), 3);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(enchanted.getId());
        harness.setLibrary(player1, List.of(new KeskitTheFleshSculptor(), new KeskitTheFleshSculptor()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(enchanted.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(other.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Aura controller's draw adds loyalty to an opponent's enchanted planeswalker")
    void controllerDrawAffectsOpponentsEnchantedPlaneswalker() {
        Permanent enchanted = addPlaneswalker(player2, new ElspethSunsChampion(), 3);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(enchanted.getId());
        harness.setLibrary(player1, List.of(new KeskitTheFleshSculptor()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(enchanted.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent drawing does not trigger the Aura")
    void opponentDrawDoesNotAddLoyalty() {
        Permanent enchanted = addPlaneswalker(player2, new ElspethSunsChampion(), 3);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(enchanted.getId());
        harness.setLibrary(player2, List.of(new KeskitTheFleshSculptor()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(enchanted.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("The granted ultimate requires twelve loyalty counters")
    void ultimateCannotBeActivatedWithInsufficientLoyalty() {
        Permanent enchanted = addPlaneswalker(player1, new ElspethSunsChampion(), 11);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(enchanted.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty counters");
        assertThat(enchanted.getCounterCount(CounterType.LOYALTY)).isEqualTo(11);
        assertThat(gd.emblems).isEmpty();
    }

    @Test
    @DisplayName("Casting the Aura attaches it to a planeswalker")
    void resolvesAttachedToPlaneswalker() {
        Permanent enchanted = addPlaneswalker(player1, new ElspethSunsChampion(), 3);
        harness.setHand(player1, List.of(new TeferisTalent()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        Permanent talent = findPermanent(player1, "Teferi's Talent");
        assertThat(talent.getAttachedTo()).isEqualTo(enchanted.getId());
    }

    @Test
    @DisplayName("The enchanted planeswalker's controller gets the emblem and can respond with loyalty abilities")
    void opponentsPlaneswalkerGrantsEmblemToItsController() {
        Permanent enchanted = addPlaneswalker(player2, new ElspethSunsChampion(), 12);
        Permanent other = addPlaneswalker(player2, new KarnLivingLegacy(), 1);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new TeferisTalent());
        talent.setAttachedTo(enchanted.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player2.getId());
        harness.assertNotOnBattlefield(player2, "Elspeth, Sun's Champion");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        Permanent own = addPlaneswalker(player1, new ElspethSunsChampion(), 3);
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(own), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(other);
        harness.activateAbility(player2, index, 0, null, null);
        Permanent later = addPlaneswalker(player2, new ElspethSunsChampion(), 3);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(later),
                0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(other.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(later.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
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
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
