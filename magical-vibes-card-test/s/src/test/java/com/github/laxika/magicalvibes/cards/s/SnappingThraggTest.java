package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.ForgottenCave;
import com.github.laxika.magicalvibes.cards.k.KrosanTusker;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnappingThragg.class, KrosanTusker.class, ForgottenCave.class, SoltariPriest.class})
class SnappingThraggTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage trigger may deal 3 damage to a creature the damaged player controls")
    void combatDamageTriggerDealsDamageToDamagedPlayersCreature() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();


        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

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

        harness.handlePermanentChosen(player1, target.getId());
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


        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

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
    @DisplayName("Does not offer a creature with protection from red")
    void cannotTargetCreatureWithProtectionFromRed() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent protectedTarget = addCreatureReady(player2, new SoltariPriest());
        Permanent validTarget = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();


        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(validTarget.getId());

        harness.handlePermanentChosen(player1, validTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(validTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(protectedTarget.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Face-down combat damage does not trigger the printed ability")
    void faceDownCombatDamageDoesNotTrigger() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Turning face up before combat damage enables the printed trigger")
    void turningFaceUpBeforeDamageEnablesTrigger() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, 0);
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 17);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger still deals damage after Snapping Thragg dies")
    void triggerResolvesAfterSourceDies() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        thragg.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Snapping Thragg");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger does not resolve when its only target has died")
    void triggerDoesNotResolveAfterTargetDies() {
        Permanent thragg = addCreatureReady(player1, new SnappingThragg());
        thragg.setAttacking(true);
        Permanent target = addCreatureReady(player2, new KrosanTusker());

        resolveCombat();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        target.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Krosan Tusker");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 17);
    }
}
