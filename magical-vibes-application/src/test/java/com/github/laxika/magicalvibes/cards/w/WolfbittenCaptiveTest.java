package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.k.KrallenhordeKiller;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.condition.NoSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WolfbittenCaptiveTest extends BaseCardTest {

    @Test
    @DisplayName("Front face has pump ability and transform trigger configured")
    void frontFaceHasCorrectEffects() {
        WolfbittenCaptive card = new WolfbittenCaptive();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getActivatedAbilities().getFirst().getMaxActivationsPerTurn()).isEqualTo(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(BoostSelfEffect.class);

        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ConditionalEffect.class);
        ConditionalEffect conditional =
                (ConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isInstanceOf(KrallenhordeKiller.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("KrallenhordeKiller");
    }

    @Test
    @DisplayName("Back face has pump ability and transform trigger configured")
    void backFaceHasCorrectEffects() {
        WolfbittenCaptive card = new WolfbittenCaptive();
        KrallenhordeKiller backFace = (KrallenhordeKiller) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).hasSize(1);
        assertThat(backFace.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}{G}");
        assertThat(backFace.getActivatedAbilities().getFirst().getMaxActivationsPerTurn()).isEqualTo(1);
        assertThat(backFace.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(BoostSelfEffect.class);

        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ConditionalEffect.class);
        ConditionalEffect conditional =
                (ConditionalEffect) backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);
    }

    @Test
    @DisplayName("Wolfbitten Captive pump ability grants +2/+2 until end of turn")
    void frontFacePumpAbilityGrantsBoost() {
        Permanent captive = addReadyWolfbittenCaptive(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(3);
    }

    @Test
    @DisplayName("Wolfbitten Captive pump ability can be activated only once each turn")
    void frontFacePumpAbilityOncePerTurn() {
        addReadyWolfbittenCaptive(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    @Test
    @DisplayName("Krallenhorde Killer pump ability grants +4/+4 until end of turn")
    void backFacePumpAbilityGrantsBoost() {
        Permanent killer = addReadyKrallenhordeKiller(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, killer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, killer)).isEqualTo(6);
    }

    @Test
    @DisplayName("Krallenhorde Killer pump ability can be activated only once each turn")
    void backFacePumpAbilityOncePerTurn() {
        addReadyKrallenhordeKiller(player1);
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    @Test
    @DisplayName("Transforms to Krallenhorde Killer when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new WolfbittenCaptive());
        Permanent captive = findPermanent(player1, "Wolfbitten Captive");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(captive.isTransformed()).isTrue();
        assertThat(captive.getCard().getName()).isEqualTo("Krallenhorde Killer");
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new WolfbittenCaptive());
        Permanent captive = findPermanent(player1, "Wolfbitten Captive");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(captive.isTransformed()).isFalse();
        assertThat(captive.getCard().getName()).isEqualTo("Wolfbitten Captive");
    }

    @Test
    @DisplayName("Krallenhorde Killer transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new WolfbittenCaptive());
        Permanent captive = findPermanent(player1, "Wolfbitten Captive");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(captive.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(captive.isTransformed()).isFalse();
        assertThat(captive.getCard().getName()).isEqualTo("Wolfbitten Captive");
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(1);
    }

    @Test
    @DisplayName("Krallenhorde Killer does not transform back when only one spell was cast last turn")
    void doesNotTransformBackWithOnlyOneSpellCastLastTurn() {
        harness.addToBattlefield(player1, new WolfbittenCaptive());
        Permanent captive = findPermanent(player1, "Wolfbitten Captive");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(captive.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(captive.isTransformed()).isTrue();
        assertThat(captive.getCard().getName()).isEqualTo("Krallenhorde Killer");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new WolfbittenCaptive());
        Permanent captive = findPermanent(player1, "Wolfbitten Captive");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(captive.isTransformed()).isTrue();
        assertThat(captive.getCard().getName()).isEqualTo("Krallenhorde Killer");
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyWolfbittenCaptive(Player player) {
        WolfbittenCaptive card = new WolfbittenCaptive();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyKrallenhordeKiller(Player player) {
        KrallenhordeKiller card = new KrallenhordeKiller();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
