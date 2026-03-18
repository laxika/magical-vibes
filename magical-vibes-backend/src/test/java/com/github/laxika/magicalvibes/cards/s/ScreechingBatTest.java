package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScreechingBatTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        ScreechingBat card = new ScreechingBat();

        assertThat(card.getActivatedAbilities()).isEmpty();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MayPayManaEffect.class);
        MayPayManaEffect mayPay = (MayPayManaEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(mayPay.manaCost()).isEqualTo("{2}{B}{B}");
        assertThat(mayPay.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("StalkingVampire");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        ScreechingBat card = new ScreechingBat();
        StalkingVampire backFace = (StalkingVampire) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).isEmpty();

        assertThat(backFace.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MayPayManaEffect.class);
        MayPayManaEffect mayPay = (MayPayManaEffect) backFace.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(mayPay.manaCost()).isEqualTo("{2}{B}{B}");
        assertThat(mayPay.wrapped()).isInstanceOf(TransformSelfEffect.class);
    }

    // ===== Transform front → back (pay mana) =====

    @Test
    @DisplayName("Transforms to Stalking Vampire when controller pays {2}{B}{B} during upkeep")
    void transformsWhenPayingMana() {
        harness.addToBattlefield(player1, new ScreechingBat());
        Permanent bat = findPermanent(player1, "Screeching Bat");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect → may prompt

        // Add mana after trigger resolves but before accepting (mana pools empty during step transitions)
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.handleMayAbilityChosen(player1, true); // accept → pay mana → transform

        assertThat(bat.isTransformed()).isTrue();
        assertThat(bat.getCard().getName()).isEqualTo("Stalking Vampire");
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when controller declines to pay")
    void doesNotTransformWhenDeclining() {
        harness.addToBattlefield(player1, new ScreechingBat());
        Permanent bat = findPermanent(player1, "Screeching Bat");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect → may prompt

        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(bat.isTransformed()).isFalse();
        assertThat(bat.getCard().getName()).isEqualTo("Screeching Bat");
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(2);
    }

    // ===== Transform back → front (Stalking Vampire pays to transform back) =====

    @Test
    @DisplayName("Stalking Vampire transforms back to Screeching Bat when controller pays during upkeep")
    void vampireTransformsBackWhenPayingMana() {
        harness.addToBattlefield(player1, new ScreechingBat());
        Permanent bat = findPermanent(player1, "Screeching Bat");

        // Transform to Stalking Vampire first
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(bat.isTransformed()).isTrue();

        // Now pay to transform back
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, back-face trigger goes on stack
        harness.passBothPriorities(); // resolve MayPayManaEffect → may prompt
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true); // accept → transform back

        assertThat(bat.isTransformed()).isFalse();
        assertThat(bat.getCard().getName()).isEqualTo("Screeching Bat");
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(2);
    }

    // ===== Does not trigger during opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new ScreechingBat());
        Permanent bat = findPermanent(player1, "Screeching Bat");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — no trigger for player1

        assertThat(bat.isTransformed()).isFalse();
        assertThat(bat.getCard().getName()).isEqualTo("Screeching Bat");
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
