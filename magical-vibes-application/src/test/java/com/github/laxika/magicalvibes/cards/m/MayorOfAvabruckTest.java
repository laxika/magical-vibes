package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlpackAlpha;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MayorOfAvabruckTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Front face has correct effects configured")
    void hasCorrectEffects() {
        MayorOfAvabruck card = new MayorOfAvabruck();

        // No activated abilities
        assertThat(card.getActivatedAbilities()).isEmpty();

        // Static boost: Other Human creatures you control get +1/+1
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        StaticBoostEffect humanBoost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(humanBoost.powerBoost()).isEqualTo(1);
        assertThat(humanBoost.toughnessBoost()).isEqualTo(1);
        assertThat(humanBoost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(humanBoost.filter()).isInstanceOf(PermanentHasSubtypePredicate.class);

        // Each-upkeep transform trigger
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("HowlpackAlpha");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        MayorOfAvabruck card = new MayorOfAvabruck();
        HowlpackAlpha backFace = (HowlpackAlpha) card.getBackFaceCard();

        // No activated abilities
        assertThat(backFace.getActivatedAbilities()).isEmpty();

        // Static boost: Each other Werewolf or Wolf you control gets +1/+1
        assertThat(backFace.getEffects(EffectSlot.STATIC)).hasSize(1);
        StaticBoostEffect wolfBoost = (StaticBoostEffect) backFace.getEffects(EffectSlot.STATIC).get(0);
        assertThat(wolfBoost.powerBoost()).isEqualTo(1);
        assertThat(wolfBoost.toughnessBoost()).isEqualTo(1);
        assertThat(wolfBoost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(wolfBoost.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);

        // Controller end step token creation
        assertThat(backFace.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect =
                (CreateTokenEffect) backFace.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(tokenEffect.tokenName()).isEqualTo("Wolf");
        assertThat(tokenEffect.power()).isEqualTo(2);
        assertThat(tokenEffect.toughness()).isEqualTo(2);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.GREEN);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.WOLF);

        // Each-upkeep transform trigger
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Howlpack Alpha when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(mayor.isTransformed()).isTrue();
        assertThat(mayor.getCard().getName()).isEqualTo("Howlpack Alpha");
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(mayor.isTransformed()).isFalse();
        assertThat(mayor.getCard().getName()).isEqualTo("Mayor of Avabruck");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Howlpack Alpha transforms back when a player cast two or more spells last turn")
    void howlpackTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mayor.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(mayor.isTransformed()).isFalse();
        assertThat(mayor.getCard().getName()).isEqualTo("Mayor of Avabruck");
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Howlpack Alpha does not transform back when only one spell was cast last turn")
    void howlpackDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mayor.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(mayor.isTransformed()).isTrue();
        assertThat(mayor.getCard().getName()).isEqualTo("Howlpack Alpha");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(mayor.isTransformed()).isTrue();
        assertThat(mayor.getCard().getName()).isEqualTo("Howlpack Alpha");
    }

    // ===== Front face lord: Other Human creatures you control get +1/+1 =====

    @Test
    @DisplayName("Other Human creatures you control get +1/+1 from front face")
    void frontFaceBoostsOtherHumans() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        // GatstafShepherd is a Human Werewolf — should get the Human boost
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        // Gatstaf Shepherd is 2/2, should be 3/3 with the +1/+1 from Mayor
        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mayor does not boost itself (says 'other')")
    void frontFaceDoesNotBoostSelf() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Mayor is 1/1, should remain 1/1 (does not boost itself)
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Human creatures do not get the front face boost")
    void frontFaceDoesNotBoostNonHumans() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        // Grizzly Bears is a Bear, not a Human
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Humans do not get the front face boost")
    void frontFaceDoesNotBoostOpponentHumans() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent oppShepherd = addCreatureReady(player2, new GatstafShepherd());

        // Opponent's Human should not get the boost
        assertThat(gqs.getEffectivePower(gd, oppShepherd)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppShepherd)).isEqualTo(2);
    }

    // ===== Back face lord: Each other Werewolf or Wolf you control gets +1/+1 =====

    @Test
    @DisplayName("Back face boosts other Werewolves you control")
    void backFaceBoostsOtherWerewolves() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mayor.isTransformed()).isTrue();

        // GatstafShepherd is a Human Werewolf — should also transform on the same upkeep
        // Add a werewolf that's already transformed (has Werewolf subtype on back face)
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        // Gatstaf Shepherd (front face) is Human Werewolf — Werewolf qualifies for the boost
        // 2/2 base + 1/1 from Howlpack Alpha = 3/3
        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Back face does not boost itself (says 'each other')")
    void backFaceDoesNotBoostSelf() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mayor.isTransformed()).isTrue();

        // Howlpack Alpha is 3/3, should remain 3/3 (does not boost itself)
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(3);
    }

    // ===== Back face: End step Wolf token creation =====

    @Test
    @DisplayName("Howlpack Alpha creates a 2/2 Wolf token at controller's end step")
    void createsWolfTokenAtEndStep() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve transform
        assertThat(mayor.isTransformed()).isTrue();

        // Advance to end step (controller's turn)
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step, token trigger goes on stack
        harness.passBothPriorities(); // resolve token creation

        // Should have created a 2/2 green Wolf token
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent wolfToken = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .findFirst().orElse(null);
        assertThat(wolfToken).isNotNull();
        assertThat(wolfToken.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, wolfToken)).isEqualTo(3);   // 2 base + 1 from Howlpack Alpha
        assertThat(gqs.getEffectiveToughness(gd, wolfToken)).isEqualTo(3); // 2 base + 1 from Howlpack Alpha
    }

    @Test
    @DisplayName("Howlpack Alpha does not create a token on opponent's end step")
    void doesNotCreateTokenOnOpponentEndStep() {
        harness.addToBattlefield(player1, new MayorOfAvabruck());
        Permanent mayor = findPermanent(player1, "Mayor of Avabruck");

        // Transform to Howlpack Alpha
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mayor.isTransformed()).isTrue();

        // Advance to opponent's end step
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step — should NOT trigger token creation

        // No Wolf token should be created
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long wolfCount = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .count();
        assertThat(wolfCount).isZero();
    }

    // ===== Helper methods =====


}
