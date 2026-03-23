package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfAsCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FellFlagshipTest extends BaseCardTest {

    // ===== Card effect configuration =====

    @Test
    @DisplayName("Has static +1/+0 boost for Pirates")
    void hasStaticPirateBoost() {
        FellFlagship card = new FellFlagship();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(0);
        assertThat(boost.scope()).isEqualTo(GrantScope.ALL_OWN_CREATURES);
        assertThat(boost.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
    }

    @Test
    @DisplayName("Has ON_COMBAT_DAMAGE_TO_PLAYER discard effect")
    void hasCombatDamageDiscardTrigger() {
        FellFlagship card = new FellFlagship();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        TargetPlayerDiscardsEffect effect =
                (TargetPlayerDiscardsEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has Crew 3 activated ability with CrewCost and AnimateSelfAsCreatureEffect")
    void hasCrewAbility() {
        FellFlagship card = new FellFlagship();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(CrewCost.class);
        assertThat(((CrewCost) ability.getEffects().get(0)).requiredPower()).isEqualTo(3);
        assertThat(ability.getEffects().get(1)).isInstanceOf(AnimateSelfAsCreatureEffect.class);
    }

    // ===== Lord effect — Pirates you control get +1/+0 =====

    @Test
    @DisplayName("Boosts Pirates you control with +1/+0")
    void boostsPirates() {
        addFellFlagshipReady(player1);
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, createPirateCard("Test Pirate"));

        assertThat(gqs.getEffectivePower(gd, pirate)).isEqualTo(3);     // 2 base + 1 lord
        assertThat(gqs.getEffectiveToughness(gd, pirate)).isEqualTo(2); // 2 base + 0 lord
    }

    @Test
    @DisplayName("Does not boost non-Pirate creatures")
    void doesNotBoostNonPirates() {
        addFellFlagshipReady(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's Pirates")
    void doesNotBoostOpponentPirates() {
        addFellFlagshipReady(player1);
        Permanent opponentPirate = harness.addToBattlefieldAndReturn(player2, createPirateCard("Opponent Pirate"));

        assertThat(gqs.getEffectivePower(gd, opponentPirate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentPirate)).isEqualTo(2);
    }

    // ===== Crew mechanic =====

    @Test
    @DisplayName("Fell Flagship is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent flagship = addFellFlagshipReady(player1);

        assertThat(gqs.isCreature(gd, flagship)).isFalse();
        assertThat(flagship.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Crewing with a single creature of sufficient power animates Fell Flagship")
    void crewWithSingleCreature() {
        Permanent flagship = addFellFlagshipReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel()); // 4/4, power >= 3

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flagship.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, flagship)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addFellFlagshipReady(player1);
        addCreatureReady(player1, new GrizzlyBears()); // power 2 < 3

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent flagship = addFellFlagshipReady(player1);
        addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, flagship)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flagship.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, flagship)).isFalse();
    }

    // ===== Combat damage trigger — that player discards a card =====

    @Test
    @DisplayName("Damaged player must discard a card when Fell Flagship deals combat damage")
    void discardOnCombatDamage() {
        Permanent flagship = addFellFlagshipReady(player1);
        flagship.setAnimatedUntilEndOfTurn(true);
        flagship.setAnimatedPower(3);
        flagship.setAnimatedToughness(3);
        flagship.setAttacking(true);

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // The combat damage trigger is on the stack — resolve it
        harness.passBothPriorities();

        // Game pauses for discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("No discard trigger when Fell Flagship is blocked")
    void noDiscardWhenBlocked() {
        Permanent flagship = addFellFlagshipReady(player1);
        flagship.setAnimatedUntilEndOfTurn(true);
        flagship.setAnimatedPower(3);
        flagship.setAnimatedToughness(3);
        flagship.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();

        // No discard prompt — Fell Flagship didn't deal combat damage to a player
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No input needed when opponent has no cards to discard")
    void noInputWhenOpponentHandEmpty() {
        Permanent flagship = addFellFlagshipReady(player1);
        flagship.setAnimatedUntilEndOfTurn(true);
        flagship.setAnimatedPower(3);
        flagship.setAnimatedToughness(3);
        flagship.setAttacking(true);

        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Discard does nothing, no input needed
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Helpers =====

    private Card createPirateCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.PIRATE));
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Permanent addFellFlagshipReady(Player player) {
        Permanent perm = new Permanent(new FellFlagship());
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

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
