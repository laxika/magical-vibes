package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeatherlightTest extends BaseCardTest {

    // ===== Card effect configuration =====

    @Test
    @DisplayName("Has ON_COMBAT_DAMAGE_TO_PLAYER effect with historic predicate")
    void hasCombatDamageTrigger() {
        Weatherlight card = new Weatherlight();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect effect =
                (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect)
                        card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(effect.count()).isEqualTo(5);
        assertThat(effect.predicate()).isInstanceOf(CardIsHistoricPredicate.class);
    }

    @Test
    @DisplayName("Has Crew 3 activated ability with CrewCost and AnimateSelfAsCreatureEffect")
    void hasCrewAbility() {
        Weatherlight card = new Weatherlight();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(CrewCost.class);
        assertThat(((CrewCost) ability.getEffects().get(0)).requiredPower()).isEqualTo(3);
        assertThat(ability.getEffects().get(1)).isInstanceOf(AnimateSelfAsCreatureEffect.class);
    }

    // ===== Crew mechanic =====

    @Test
    @DisplayName("Weatherlight is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent weatherlight = addWeatherlightReady(player1);

        assertThat(gqs.isCreature(gd, weatherlight)).isFalse();
        assertThat(weatherlight.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Crewing with a single creature of sufficient power animates Weatherlight")
    void crewWithSingleCreature() {
        Permanent weatherlight = addWeatherlightReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel()); // 4/4, power >= 3

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weatherlight.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, weatherlight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, weatherlight)).isEqualTo(5);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crewing with multiple small creatures (total power >= 3) works")
    void crewWithMultipleCreatures() {
        Permanent weatherlight = addWeatherlightReady(player1);
        // Two 2/2 creatures — total power 4 >= 3
        Permanent bear1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weatherlight.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(bear1.isTapped()).isTrue();
        assertThat(bear2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addWeatherlightReady(player1);
        // Single 2/2 creature — power 2 < 3
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Cannot crew when no creatures are available")
    void cannotCrewWithNoCreatures() {
        addWeatherlightReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent weatherlight = addWeatherlightReady(player1);
        addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(weatherlight.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, weatherlight)).isFalse();
    }

    @Test
    @DisplayName("Crew does not require the vehicle to tap")
    void crewDoesNotTapVehicle() {
        Permanent weatherlight = addWeatherlightReady(player1);
        addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weatherlight.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures cannot be used to crew")
    void tappedCreaturesCannotCrew() {
        addWeatherlightReady(player1);
        Permanent creature = addCreatureReady(player1, new SerraAngel());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Deals combat damage and triggers look at top 5 with historic filter")
    void combatDamageTriggerWorks() {
        harness.setLife(player2, 20);
        Permanent weatherlight = addWeatherlightReady(player1);
        // Manually animate it as if crewed
        weatherlight.setAnimatedUntilEndOfTurn(true);
        weatherlight.setAnimatedPower(4);
        weatherlight.setAnimatedToughness(5);
        weatherlight.setAttacking(true);

        setupTopCards(List.of(
                new ArvadTheCursed(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Weatherlight deals 4 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // The combat damage trigger is on the stack — resolve it
        harness.passBothPriorities();

        // Trigger should have resolved — offering the legendary creature
        GameData gdAfter = harness.getGameData();
        assertThat(gdAfter.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gdAfter.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gdAfter.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Arvad the Cursed");
    }

    @Test
    @DisplayName("Combat damage trigger does not fire when blocked")
    void noCombatDamageTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent weatherlight = addWeatherlightReady(player1);
        weatherlight.setAnimatedUntilEndOfTurn(true);
        weatherlight.setAnimatedPower(4);
        weatherlight.setAnimatedToughness(5);
        weatherlight.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        setupTopCards(List.of(
                new ArvadTheCursed(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // No combat damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // No trigger fired
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Helpers =====

    private Permanent addWeatherlightReady(Player player) {
        Permanent perm = new Permanent(new Weatherlight());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
