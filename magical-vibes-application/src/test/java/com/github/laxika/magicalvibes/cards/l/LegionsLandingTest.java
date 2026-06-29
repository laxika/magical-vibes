package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AdantoTheFirstFort;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MinimumAttackersConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LegionsLandingTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Front face has ETB CreateTokenEffect for 1/1 white Vampire with lifelink")
    void frontFaceHasCorrectETBEffect() {
        LegionsLanding card = new LegionsLanding();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect token = (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(token.tokenName()).isEqualTo("Vampire");
        assertThat(token.power()).isEqualTo(1);
        assertThat(token.toughness()).isEqualTo(1);
        assertThat(token.color()).isEqualTo(CardColor.WHITE);
        assertThat(token.subtypes()).containsExactly(CardSubtype.VAMPIRE);
        assertThat(token.keywords()).containsExactly(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Front face has ON_ALLY_CREATURES_ATTACK with MinimumAttackersConditionalEffect(3) wrapping TransformSelfEffect")
    void frontFaceHasCorrectAttackTransformTrigger() {
        LegionsLanding card = new LegionsLanding();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURES_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURES_ATTACK).getFirst())
                .isInstanceOf(MinimumAttackersConditionalEffect.class);
        MinimumAttackersConditionalEffect mac = (MinimumAttackersConditionalEffect)
                card.getEffects(EffectSlot.ON_ALLY_CREATURES_ATTACK).getFirst();
        assertThat(mac.minimumAttackers()).isEqualTo(3);
        assertThat(mac.wrapped()).isInstanceOf(TransformSelfEffect.class);
    }

    @Test
    @DisplayName("Front face has back face linked")
    void frontFaceHasBackFace() {
        LegionsLanding card = new LegionsLanding();

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard()).isInstanceOf(AdantoTheFirstFort.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("AdantoTheFirstFort");
    }

    @Test
    @DisplayName("Back face has two activated abilities: tap for {W} and {2}{W},{T} for vampire token")
    void backFaceHasCorrectAbilities() {
        LegionsLanding card = new LegionsLanding();
        AdantoTheFirstFort backFace = (AdantoTheFirstFort) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).hasSize(2);

        // {T}: Add {W}.
        var tapForWhite = backFace.getActivatedAbilities().get(0);
        assertThat(tapForWhite.isRequiresTap()).isTrue();
        assertThat(tapForWhite.getManaCost()).isNull();
        assertThat(tapForWhite.getEffects()).hasSize(1);
        assertThat(tapForWhite.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) tapForWhite.getEffects().getFirst()).color()).isEqualTo(ManaColor.WHITE);

        // {2}{W}, {T}: Create a 1/1 white Vampire creature token with lifelink.
        var makeVampire = backFace.getActivatedAbilities().get(1);
        assertThat(makeVampire.isRequiresTap()).isTrue();
        assertThat(makeVampire.getManaCost()).isEqualTo("{2}{W}");
        assertThat(makeVampire.getEffects()).hasSize(1);
        assertThat(makeVampire.getEffects().getFirst()).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect token = (CreateTokenEffect) makeVampire.getEffects().getFirst();
        assertThat(token.tokenName()).isEqualTo("Vampire");
        assertThat(token.power()).isEqualTo(1);
        assertThat(token.toughness()).isEqualTo(1);
        assertThat(token.color()).isEqualTo(CardColor.WHITE);
        assertThat(token.subtypes()).containsExactly(CardSubtype.VAMPIRE);
        assertThat(token.keywords()).containsExactly(Keyword.LIFELINK);
    }

    // ===== ETB: create vampire token =====

    @Test
    @DisplayName("ETB creates a 1/1 white Vampire token with lifelink")
    void etbCreatesVampireToken() {
        harness.setHand(player1, List.of(new LegionsLanding()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment, triggers ETB
        harness.passBothPriorities(); // resolve ETB trigger

        // Should have a Vampire token on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    // ===== Attack transform trigger =====

    @Test
    @DisplayName("Transforms when attacking with exactly 3 creatures")
    void transformsWithThreeAttackers() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2, 3)); // indices 1,2,3 are the creatures (0 is Landing)
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(landing.isTransformed()).isTrue();
        assertThat(landing.getCard().getName()).isEqualTo("Adanto, the First Fort");
    }

    @Test
    @DisplayName("Transforms when attacking with more than 3 creatures")
    void transformsWithFourAttackers() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2, 3, 4));
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(landing.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when attacking with only 2 creatures")
    void doesNotTransformWithTwoAttackers() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1, 2));

        assertThat(landing.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not transform when attacking with only 1 creature")
    void doesNotTransformWithOneAttacker() {
        Permanent landing = addLandingReady(player1);
        addCreatureReady(player1);

        declareAttackers(List.of(1));

        assertThat(landing.isTransformed()).isFalse();
    }

    // ===== Back face: Adanto mana abilities =====

    @Test
    @DisplayName("Adanto tap ability adds one white mana")
    void adantoTapAddsWhiteMana() {
        Permanent adanto = addTransformedAdanto(player1);

        int adantoIdx = indexOf(player1, adanto);
        harness.activateAbility(player1, adantoIdx, 0, null, null);
        // Mana ability resolves immediately (no stack) — don't pass priorities
        // which would advance steps and drain the mana pool

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Adanto activated ability creates a 1/1 Vampire token with lifelink")
    void adantoActivatedAbilityCreatesVampireToken() {
        Permanent adanto = addTransformedAdanto(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int adantoIdx = indexOf(player1, adanto);
        harness.activateAbility(player1, adantoIdx, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vampire")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    // ===== Helpers =====

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player1, attackerIndices);
    }

    private Permanent addLandingReady(Player player) {
        LegionsLanding card = new LegionsLanding();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedAdanto(Player player) {
        LegionsLanding card = new LegionsLanding();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{W}");
        creature.setColor(CardColor.WHITE);
        creature.setPower(2);
        creature.setToughness(2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
