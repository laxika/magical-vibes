package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.ForgottenCave;
import com.github.laxika.magicalvibes.cards.k.KrosanTusker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnappingThragg.class, KrosanTusker.class, ForgottenCave.class})
class SnappingThraggTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger may deal 3 damage to a creature the damaged player controls")
    void combatDamageTriggerDealsDamageToDamagedPlayersCreature() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the combat damage trigger deals no additional damage")
    void decliningCombatDamageTriggerDealsNoAdditionalDamage() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Morphs face down and can be turned face up")
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new SnappingThragg()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent thragg = findPermanent(player1, "Snapping Thragg");
        assertThat(thragg.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(thragg));
        harness.passBothPriorities();

        assertThat(thragg.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Only offers creatures controlled by the damaged player")
    void onlyOffersCreaturesControlledByDamagedPlayer() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new KrosanTusker());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new ForgottenCave());
        Permanent target = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(nonCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not create the optional ability without a legal creature target")
    void doesNotCreateOptionalAbilityWithoutLegalCreatureTarget() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(SoltariPriest.class)
    @DisplayName("Does not offer a creature with protection from red")
    void cannotTargetCreatureWithProtectionFromRed() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent protectedTarget = addCreatureReady(player2, new SoltariPriest());
        Permanent validTarget = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(validTarget.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(validTarget.getId()));

        assertThat(validTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(protectedTarget.getMarkedDamage()).isZero();
    }
}
