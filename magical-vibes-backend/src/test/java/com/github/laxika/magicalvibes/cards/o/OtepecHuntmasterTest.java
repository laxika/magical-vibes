package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtepecHuntmasterTest extends BaseCardTest {

    private void addHuntmasterReady() {
        harness.addToBattlefield(player1, new OtepecHuntmaster());
        Permanent huntmaster = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Otepec Huntmaster"))
                .findFirst().orElseThrow();
        huntmaster.setSummoningSick(false);
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Has cost reduction static effect for Dinosaurs")
    void hasCorrectStaticEffect() {
        OtepecHuntmaster card = new OtepecHuntmaster();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(ReduceOwnCastCostForSubtypeEffect.class);

        ReduceOwnCastCostForSubtypeEffect effect = (ReduceOwnCastCostForSubtypeEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(effect.affectedSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(effect.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has tap activated ability that grants haste to target Dinosaur")
    void hasCorrectActivatedAbility() {
        OtepecHuntmaster card = new OtepecHuntmaster();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        ActivatedAbility ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(GrantKeywordEffect.class);

        GrantKeywordEffect grant = (GrantKeywordEffect) ability.getEffects().getFirst();
        assertThat(grant.keywords()).containsExactly(Keyword.HASTE);
        assertThat(grant.scope()).isEqualTo(GrantScope.TARGET);

        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
    }

    // ===== Cost reduction =====

    @Test
    @DisplayName("Dinosaur spells cost {1} less to cast")
    void dinosaurSpellsCostOneLess() {
        harness.addToBattlefield(player1, new OtepecHuntmaster());
        // Frenzied Raptor costs {2}{R} — with {1} reduction it should cost {1}{R}
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Frenzied Raptor");
    }

    @Test
    @DisplayName("Cannot cast Dinosaur without enough mana even with cost reduction")
    void cannotCastDinosaurWithoutEnoughMana() {
        harness.addToBattlefield(player1, new OtepecHuntmaster());
        // Frenzied Raptor costs {2}{R} — with {1} reduction needs {1}{R}; only {R} is not enough
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Dinosaur creature spells are not reduced")
    void nonDinosaurSpellsNotReduced() {
        harness.addToBattlefield(player1, new OtepecHuntmaster());
        // Grizzly Bears costs {1}{G} — should not be reduced
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Only {G} is not enough for {1}{G}
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cost reduction does not apply to opponent's Dinosaur spells")
    void doesNotReduceOpponentDinosaurCosts() {
        harness.addToBattlefield(player1, new OtepecHuntmaster());
        // Opponent's Frenzied Raptor should still cost {2}{R}
        harness.setHand(player2, List.of(new FrenziedRaptor()));
        harness.addMana(player2, ManaColor.RED, 2);

        // Only {R}{R} is not enough for {2}{R} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two Otepec Huntmasters reduce Dinosaur spell cost by {2}")
    void twoHuntmastersStackReduction() {
        harness.addToBattlefield(player1, new OtepecHuntmaster());
        harness.addToBattlefield(player1, new OtepecHuntmaster());
        // Frenzied Raptor costs {2}{R} — with {2} reduction it should cost just {R}
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Frenzied Raptor");
    }

    // ===== Tap ability: grant haste =====

    @Test
    @DisplayName("Tap ability grants haste to target Dinosaur until end of turn")
    void tapAbilityGrantsHasteToDinosaur() {
        addHuntmasterReady();
        harness.addToBattlefield(player1, new FrenziedRaptor());

        UUID targetId = harness.getPermanentId(player1, "Frenzied Raptor");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent raptor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();
        assertThat(raptor.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Tap ability cannot target non-Dinosaur creature")
    void cannotTargetNonDinosaur() {
        addHuntmasterReady();
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate tap ability with summoning sickness")
    void respectsSummoningSickness() {
        harness.addToBattlefield(player1, new OtepecHuntmaster());
        harness.addToBattlefield(player1, new FrenziedRaptor());

        UUID targetId = harness.getPermanentId(player1, "Frenzied Raptor");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate tap ability when already tapped")
    void cannotActivateWhenTapped() {
        addHuntmasterReady();
        harness.addToBattlefield(player1, new FrenziedRaptor());

        Permanent huntmaster = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Otepec Huntmaster"))
                .findFirst().orElseThrow();
        huntmaster.tap();

        UUID targetId = harness.getPermanentId(player1, "Frenzied Raptor");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability can target opponent's Dinosaur")
    void canTargetOpponentDinosaur() {
        addHuntmasterReady();
        harness.addToBattlefield(player2, new FrenziedRaptor());

        UUID targetId = harness.getPermanentId(player2, "Frenzied Raptor");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent raptor = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frenzied Raptor"))
                .findFirst().orElseThrow();
        assertThat(raptor.getGrantedKeywords()).contains(Keyword.HASTE);
    }
}
