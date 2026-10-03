package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TempleOfMalice;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.r.RetractionHelix;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArbiterOfTheIdeal.class, AstralCornucopia.class, Solemnity.class, Forest.class,
        RetractionHelix.class, DoublingSeason.class, AjanisChosen.class, TempleOfMalice.class})
class ArbiterOfTheIdealTest extends BaseCardTest {

    @Test
    @DisplayName("Inspired puts a matching top card onto the battlefield with its counter and enchantment type")
    void putsMatchingCardOntoBattlefieldWithModifications() {
        Permanent arbiter = addTappedArbiter(player1);
        Card ring = new AstralCornucopia();
        harness.setLibrary(player1, List.of(ring));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(ring.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.MANIFESTATION)).isEqualTo(1);
        assertThat(gqs.isEnchantment(gd, entered)).isTrue();
        assertThat(arbiter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Solemnity prevents the manifestation counter but not the enchantment type")
    void solemnityPreventsManifestationCounter() {
        addTappedArbiter(player1);
        harness.addToBattlefield(player1, new Solemnity());
        Card ring = new AstralCornucopia();
        harness.setLibrary(player1, List.of(ring));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(ring.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.getCounterCount(CounterType.MANIFESTATION)).isZero();
        assertThat(gqs.isEnchantment(gd, entered)).isTrue();
    }

    @Test
    @DisplayName("Inspired leaves a nonmatching top card available to draw without offering a choice")
    void nonmatchingCardStaysOnTop() {
        addTappedArbiter(player1);
        Card nonmatching = new RetractionHelix();
        harness.setLibrary(player1, List.of(nonmatching));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(nonmatching.getId()));
        harness.assertNotOnBattlefield(player1, "Retraction Helix");
    }

    @Test
    @DisplayName("Declining Inspired leaves the matching top card available to draw")
    void declineLeavesMatchingCardOnTop() {
        addTappedArbiter(player1);
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(land.getId()));
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Doubling Season doubles the manifestation counter placed on entry")
    void doublesManifestationCounter() {
        addTappedArbiter(player1);
        harness.addToBattlefield(player1, new DoublingSeason());
        Card ring = new AstralCornucopia();
        harness.setLibrary(player1, List.of(ring));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(ring.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.MANIFESTATION)).isEqualTo(2);
        assertThat(gqs.isEnchantment(gd, entered)).isTrue();
    }

    @Test
    @DisplayName("Inspired puts a creature onto the battlefield without casting it")
    void putsCreatureOntoBattlefield() {
        addTappedArbiter(player1);
        Card creature = new ArbiterOfTheIdeal();
        harness.setLibrary(player1, List.of(creature));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.MANIFESTATION)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, entered)).isTrue();
        assertThat(gqs.isEnchantment(gd, entered)).isTrue();
        assertThat(entered.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Inspired puts a land onto the battlefield as an enchantment")
    void putsLandOntoBattlefield() {
        addTappedArbiter(player1);
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(land.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.MANIFESTATION)).isEqualTo(1);
        assertThat(gqs.isEnchantment(gd, entered)).isTrue();
        assertThat(gqs.isLand(gd, entered)).isTrue();
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Removing the manifestation counter does not remove the enchantment type")
    void enchantmentTypeDoesNotDependOnCounter() {
        addTappedArbiter(player1);
        Card ring = new AstralCornucopia();
        harness.setLibrary(player1, List.of(ring));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(ring.getId()))
                .findFirst().orElseThrow();
        entered.setCounterCount(CounterType.MANIFESTATION, 0);
        assertThat(gqs.isEnchantment(gd, entered)).isTrue();
    }

    @Test
    @DisplayName("A creature put onto the battlefield by Inspired triggers enchantment entry abilities")
    void modifiedCreatureTriggersEnchantmentEntryAbility() {
        addTappedArbiter(player1);
        harness.addToBattlefield(player1, new AjanisChosen());
        Card creature = new ArbiterOfTheIdeal();
        harness.setLibrary(player1, List.of(creature, new Forest(), new Forest()));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Cat"));
    }

    @Test
    @DisplayName("A land put onto the battlefield by Inspired still triggers its own entry ability")
    void landEntryTriggersScry() {
        addTappedArbiter(player1);
        Card land = new TempleOfMalice();
        harness.setLibrary(player1, List.of(land, new RetractionHelix(), new ArbiterOfTheIdeal()));

        resolveUntapTrigger(player1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.UPKEEP, () -> harness.handleMayAbilityChosen(player1, true));
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(land.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    private Permanent addTappedArbiter(Player player) {
        Permanent arbiter = harness.addToBattlefieldAndReturn(player, new ArbiterOfTheIdeal());
        arbiter.tap();
        return arbiter;
    }

    private void resolveUntapTrigger(Player activePlayer) {
        Player opponent = activePlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }

}
