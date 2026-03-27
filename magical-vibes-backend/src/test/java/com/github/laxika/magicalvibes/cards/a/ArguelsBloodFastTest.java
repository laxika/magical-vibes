package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TempleOfAclazotz;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToXValueEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArguelsBloodFastTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Front face has correct effects configured")
    void frontFaceHasCorrectEffects() {
        ArguelsBloodFast card = new ArguelsBloodFast();

        // One activated ability: {1}{B}, Pay 2 life: Draw a card
        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{1}{B}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(PayLifeCost.class);
        assertThat(((PayLifeCost) ability.getEffects().get(0)).amount()).isEqualTo(2);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DrawCardEffect.class);

        // Upkeep trigger: if you have 5 or less life, you may transform
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ControllerLifeAtOrBelowThresholdConditionalEffect.class);
        var conditional = (ControllerLifeAtOrBelowThresholdConditionalEffect)
                card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.lifeThreshold()).isEqualTo(5);
        assertThat(conditional.wrapped()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) conditional.wrapped();
        assertThat(may.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("TempleOfAclazotz");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        ArguelsBloodFast card = new ArguelsBloodFast();
        TempleOfAclazotz backFace = (TempleOfAclazotz) card.getBackFaceCard();

        // {T}: Add {B}
        assertThat(backFace.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.ON_TAP).getFirst()).isInstanceOf(AwardManaEffect.class);

        // Activated ability: {T}, Sacrifice a creature: gain life equal to toughness
        assertThat(backFace.getActivatedAbilities()).hasSize(1);
        var ability = backFace.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        SacrificeCreatureCost sacCost = (SacrificeCreatureCost) ability.getEffects().get(0);
        assertThat(sacCost.trackSacrificedToughness()).isTrue();
        assertThat(ability.getEffects().get(1)).isInstanceOf(GainLifeEqualToXValueEffect.class);
    }

    // ===== Front face: {1}{B}, Pay 2 life: Draw a card =====

    @Test
    @DisplayName("Activated ability draws a card and costs 2 life")
    void activatedAbilityDrawsCardAndCostsLife() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        Card cardInLibrary = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, cardInLibrary);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addEnchantmentReady(player1);
        harness.setLife(player1, 20);
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability without enough life")
    void cannotActivateWithoutEnoughLife() {
        addEnchantmentReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 1); // Only 1 life, need 2

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation at exactly 2 life is accepted but player loses before ability resolves (CR 704.5a)")
    void canActivateWithExactly2Life() {
        addEnchantmentReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        Card cardInLibrary = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, cardInLibrary);

        // Activation is accepted (2 >= 2), life cost is paid, but SBAs fire immediately
        // and the player loses at 0 life before the ability resolves (CR 704.3 / 704.5a)
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    // ===== Upkeep trigger: transform at 5 or less life =====

    @Test
    @DisplayName("Transforms when accepting may at 5 life during upkeep")
    void transformsAtFiveLife() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability — queues may prompt
        harness.handleMayAbilityChosen(player1, true); // accept transform

        assertThat(enchantment.isTransformed()).isTrue();
        assertThat(enchantment.getCard().getName()).isEqualTo("Temple of Aclazotz");
    }

    @Test
    @DisplayName("Does not transform when declining may at 5 life")
    void doesNotTransformWhenDeclined() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, false); // decline transform

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(enchantment.getCard().getName()).isEqualTo("Arguel's Blood Fast");
    }

    @Test
    @DisplayName("Does not trigger at 6 or more life")
    void doesNotTriggerAboveFiveLife() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers at 1 life")
    void triggersAtOneLife() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, true);

        assertThat(enchantment.isTransformed()).isTrue();
    }

    // ===== Back face: Temple of Aclazotz =====

    @Test
    @DisplayName("Temple of Aclazotz sacrifice ability gains life equal to toughness")
    void templeGainsLifeEqualToToughness() {
        // Set up a transformed Arguel's Blood Fast (which is Temple of Aclazotz)
        Permanent temple = addTransformedTemple(player1);
        // Only one creature — auto-sacrificed (Temple is a land, not a creature)
        Permanent creature = addCreatureReady(player1, createCreature("Beefy Beast", 2, 4));
        harness.setLife(player1, 10);

        int templeIdx = indexOf(player1, temple);
        harness.activateAbility(player1, templeIdx, 0, null, null);
        harness.passBothPriorities();

        // Gained 4 life (creature toughness)
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        // Creature should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beefy Beast"));
    }

    @Test
    @DisplayName("Temple sacrifice with 1/1 creature gains 1 life")
    void templeSacrifice1_1GainsOneLife() {
        Permanent temple = addTransformedTemple(player1);
        // Only one creature — auto-sacrificed
        Permanent token = addCreatureReady(player1, createCreature("Goblin Token", 1, 1));
        harness.setLife(player1, 10);

        int templeIdx = indexOf(player1, temple);
        harness.activateAbility(player1, templeIdx, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }

    // ===== Helpers =====

    private Permanent addEnchantmentReady(Player player) {
        ArguelsBloodFast card = new ArguelsBloodFast();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedTemple(Player player) {
        ArguelsBloodFast card = new ArguelsBloodFast();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        // Transform to back face
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
