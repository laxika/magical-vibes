package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.k.KrallenhordeKiller;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfbittenCaptive.class, KrallenhordeKiller.class})
class WolfbittenCaptiveTest extends BaseCardTest {

    

    

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
                .hasMessageContaining("only once each turn");
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
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Transforms to Krallenhorde Killer when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent captive = harness.addToBattlefieldAndReturn(player1, new WolfbittenCaptive());

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
        Permanent captive = harness.addToBattlefieldAndReturn(player1, new WolfbittenCaptive());

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
        Permanent captive = harness.addToBattlefieldAndReturn(player1, new WolfbittenCaptive());

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
        Permanent captive = harness.addToBattlefieldAndReturn(player1, new WolfbittenCaptive());

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
        Permanent captive = harness.addToBattlefieldAndReturn(player1, new WolfbittenCaptive());

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(captive.isTransformed()).isTrue();
        assertThat(captive.getCard().getName()).isEqualTo("Krallenhorde Killer");
    }

    @Test
    @DisplayName("Both faces can be pumped in the same upkeep, front face first")
    void bothFacesCanBeActivatedInSameTurnFrontFirst() {
        Permanent captive = addReadyWolfbittenCaptive(player1);
        gd.spellsCastLastTurn.clear();
        harness.forceStep(TurnStep.UNTAP);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(captive.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(8);
    }

    @Test
    @DisplayName("Both faces can be pumped in the same upkeep, back face first")
    void bothFacesCanBeActivatedInSameTurnBackFirst() {
        Permanent captive = addReadyKrallenhordeKiller(player1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(6);
        harness.passBothPriorities();
        assertThat(captive.isTransformed()).isFalse();
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(7);
    }

    @Test
    @DisplayName("Front face pump expires and can be activated again on the next turn")
    void frontFacePumpExpiresAndResetsNextTurn() {
        Permanent captive = addReadyWolfbittenCaptive(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(3);
    }

    @Test
    @DisplayName("Back face pump expires and can be activated again on the next turn")
    void backFacePumpExpiresAndResetsNextTurn() {
        Permanent captive = addReadyKrallenhordeKiller(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(2);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(6);
    }

    @Test
    @DisplayName("Pump can be activated while tapped and summoning sick")
    void pumpDoesNotRequireUntappedOrReadyCreature() {
        Permanent captive = harness.addToBattlefieldAndReturn(player1, new WolfbittenCaptive());
        captive.setTapped(true);
        captive.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, captive)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, captive)).isEqualTo(3);
        assertThat(captive.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once-per-turn restriction applies before the first activation resolves")
    void cannotActivateFrontPumpTwiceWhileFirstActivationIsPending() {
        addReadyWolfbittenCaptive(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyWolfbittenCaptive(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WolfbittenCaptive());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyKrallenhordeKiller(Player player) {
        Permanent perm = addReadyWolfbittenCaptive(player);
        perm.setCard(perm.getOriginalCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }
}
