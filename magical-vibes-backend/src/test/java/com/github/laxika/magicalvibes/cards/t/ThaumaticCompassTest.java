package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SpiresOfOrazca;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThaumaticCompassTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Front face has activated ability to search for basic land")
    void frontFaceHasSearchAbility() {
        ThaumaticCompass card = new ThaumaticCompass();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{3}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(SearchLibraryForBasicLandToHandEffect.class);
    }

    @Test
    @DisplayName("Front face has end step transform trigger for 7+ lands")
    void frontFaceHasEndStepTransformTrigger() {
        ThaumaticCompass card = new ThaumaticCompass();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(ControlsPermanentCountConditionalEffect.class);
        var conditional = (ControlsPermanentCountConditionalEffect)
                card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(conditional.minCount()).isEqualTo(7);
        assertThat(conditional.filter()).isInstanceOf(PermanentIsLandPredicate.class);
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);
    }

    @Test
    @DisplayName("Front face has back face linked")
    void frontFaceHasBackFace() {
        ThaumaticCompass card = new ThaumaticCompass();

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard()).isInstanceOf(SpiresOfOrazca.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("SpiresOfOrazca");
    }

    @Test
    @DisplayName("Back face has colorless mana ability and combat removal ability")
    void backFaceHasCorrectAbilities() {
        ThaumaticCompass card = new ThaumaticCompass();
        SpiresOfOrazca backFace = (SpiresOfOrazca) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).hasSize(2);

        // {T}: Add {C}.
        var manaAbility = backFace.getActivatedAbilities().get(0);
        assertThat(manaAbility.isRequiresTap()).isTrue();
        assertThat(manaAbility.getManaCost()).isNull();
        assertThat(manaAbility.getEffects()).hasSize(1);
        assertThat(manaAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) manaAbility.getEffects().getFirst()).color()).isEqualTo(ManaColor.COLORLESS);

        // {T}: Untap target attacking creature an opponent controls and remove it from combat.
        var combatAbility = backFace.getActivatedAbilities().get(1);
        assertThat(combatAbility.isRequiresTap()).isTrue();
        assertThat(combatAbility.getManaCost()).isNull();
        assertThat(combatAbility.getEffects()).hasSize(2);
        assertThat(combatAbility.getEffects().get(0)).isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(combatAbility.getEffects().get(1)).isInstanceOf(RemoveTargetFromCombatEffect.class);
        assertThat(combatAbility.getTargetFilter()).isNotNull();
    }

    // ===== Activated ability: search for basic land =====

    @Test
    @DisplayName("Search ability finds basic land and puts it in hand")
    void searchAbilityFindsBasicLand() {
        Card basicLand = createBasicLand("Forest");
        Card nonLand = createCreature("Bear", 2, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(nonLand, basicLand));

        Permanent compass = addArtifactReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int idx = indexOf(player1, compass);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities(); // resolve ability

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Forest");
    }

    // ===== End step transform =====

    @Test
    @DisplayName("Transforms at end step with exactly 7 lands")
    void transformsWithSevenLands() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 7; i++) {
            addLandReady(player1);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step, trigger goes on stack
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(compass.isTransformed()).isTrue();
        assertThat(compass.getCard().getName()).isEqualTo("Spires of Orazca");
    }

    @Test
    @DisplayName("Transforms at end step with more than 7 lands")
    void transformsWithEightLands() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 8; i++) {
            addLandReady(player1);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(compass.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at end step with only 6 lands")
    void doesNotTransformWithSixLands() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 6; i++) {
            addLandReady(player1);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(compass.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent compass = addArtifactReady(player1);
        for (int i = 0; i < 7; i++) {
            addLandReady(player1);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(compass.isTransformed()).isFalse();
    }

    // ===== Back face: Spires of Orazca mana ability =====

    @Test
    @DisplayName("Spires of Orazca tap adds one colorless mana")
    void spiresBasicTapAddsColorless() {
        Permanent spires = addTransformedSpires(player1);

        int spiresIdx = indexOf(player1, spires);
        harness.activateAbility(player1, spiresIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    // ===== Back face: untap and remove from combat =====

    @Test
    @DisplayName("Spires untaps and removes opponent's attacking creature from combat")
    void spiresUntapsAndRemovesAttacker() {
        Permanent spires = addTransformedSpires(player1);

        Card bear = createCreature("Bear", 2, 2);
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        attacker.tap();
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int spiresIdx = indexOf(player1, spires);
        harness.activateAbility(player1, spiresIdx, 1, null, attacker.getId());
        harness.passBothPriorities(); // resolve ability

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Spires cannot target own attacking creature")
    void spiresCannotTargetOwnCreature() {
        Permanent spires = addTransformedSpires(player1);

        Card bear = createCreature("Bear", 2, 2);
        Permanent ownAttacker = new Permanent(bear);
        ownAttacker.setSummoningSick(false);
        ownAttacker.setAttacking(true);
        ownAttacker.setAttackTarget(player2.getId());
        ownAttacker.tap();
        gd.playerBattlefields.get(player1.getId()).add(ownAttacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int spiresIdx = indexOf(player1, spires);
        assertThatThrownBy(() -> harness.activateAbility(player1, spiresIdx, 1, null, ownAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spires cannot target non-attacking creature")
    void spiresCannotTargetNonAttacker() {
        Permanent spires = addTransformedSpires(player1);

        Card bear = createCreature("Bear", 2, 2);
        Permanent nonAttacker = new Permanent(bear);
        nonAttacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(nonAttacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int spiresIdx = indexOf(player1, spires);
        assertThatThrownBy(() -> harness.activateAbility(player1, spiresIdx, 1, null, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addArtifactReady(Player player) {
        ThaumaticCompass card = new ThaumaticCompass();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedSpires(Player player) {
        ThaumaticCompass card = new ThaumaticCompass();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLandReady(Player player) {
        Card land = new Card();
        land.setName("Forest");
        land.setType(CardType.LAND);
        land.setSupertypes(Set.of(CardSupertype.BASIC));
        Permanent perm = new Permanent(land);
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

    private Card createBasicLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        card.setSupertypes(Set.of(CardSupertype.BASIC));
        return card;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
