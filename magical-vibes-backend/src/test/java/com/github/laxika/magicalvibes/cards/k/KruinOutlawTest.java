package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TerrorOfKruinPass;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class KruinOutlawTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        KruinOutlaw card = new KruinOutlaw();

        // No activated abilities
        assertThat(card.getActivatedAbilities()).isEmpty();

        // No static effects on front face
        assertThat(card.getEffects(EffectSlot.STATIC)).isEmpty();

        // Each-upkeep transform trigger
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect conditional =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("TerrorOfKruinPass");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        KruinOutlaw card = new KruinOutlaw();
        TerrorOfKruinPass backFace = (TerrorOfKruinPass) card.getBackFaceCard();

        // No activated abilities
        assertThat(backFace.getActivatedAbilities()).isEmpty();

        // Static: Werewolves you control have menace (ALL_OWN_CREATURES includes self)
        assertThat(backFace.getEffects(EffectSlot.STATIC)).hasSize(1);
        StaticBoostEffect menaceBoost = (StaticBoostEffect) backFace.getEffects(EffectSlot.STATIC).get(0);
        assertThat(menaceBoost.powerBoost()).isEqualTo(0);
        assertThat(menaceBoost.toughnessBoost()).isEqualTo(0);
        assertThat(menaceBoost.grantedKeywords()).isEqualTo(Set.of(Keyword.MENACE));
        assertThat(menaceBoost.scope()).isEqualTo(GrantScope.ALL_OWN_CREATURES);
        assertThat(menaceBoost.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);

        // Each-upkeep transform trigger
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    // ===== Werewolf transform: front → back (no spells cast last turn) =====

    @Test
    @DisplayName("Transforms to Terror of Kruin Pass when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
        assertThat(gqs.getEffectivePower(gd, outlaw)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outlaw)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(outlaw.isTransformed()).isFalse();
        assertThat(outlaw.getCard().getName()).isEqualTo("Kruin Outlaw");
    }

    // ===== Werewolf transform: back → front (two or more spells cast last turn) =====

    @Test
    @DisplayName("Terror of Kruin Pass transforms back when a player cast two or more spells last turn")
    void terrorTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve transform back

        assertThat(outlaw.isTransformed()).isFalse();
        assertThat(outlaw.getCard().getName()).isEqualTo("Kruin Outlaw");
        assertThat(gqs.getEffectivePower(gd, outlaw)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, outlaw)).isEqualTo(2);
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not transform back when only one spell was cast last turn")
    void terrorDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Only 1 spell cast last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
    }

    // ===== Transform triggers on every upkeep (not just controller's) =====

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger fires
        harness.passBothPriorities(); // resolve

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
    }

    // ===== Static menace: Werewolves you control have menace (back face) =====

    @Test
    @DisplayName("Terror of Kruin Pass grants menace to other werewolves you control")
    void backFaceGrantsMenaceToOtherWerewolves() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add another werewolf
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, shepherd, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Terror of Kruin Pass itself has menace")
    void backFaceHasMenaceItself() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");
        outlaw.setSummoningSick(false);

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not grant menace to non-werewolf creatures")
    void backFaceDoesNotGrantMenaceToNonWerewolves() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add a non-werewolf creature
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not grant menace to opponent's werewolves")
    void backFaceDoesNotGrantMenaceToOpponentWerewolves() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent outlaw = findPermanent(player1, "Kruin Outlaw");

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add a werewolf to opponent's battlefield
        Permanent oppShepherd = addCreatureReady(player2, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, oppShepherd, Keyword.MENACE)).isFalse();
    }

    // ===== Front face does not grant menace =====

    @Test
    @DisplayName("Front face Kruin Outlaw does not grant menace to werewolves")
    void frontFaceDoesNotGrantMenace() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, shepherd, Keyword.MENACE)).isFalse();
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
