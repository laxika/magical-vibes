package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.b.BurnTrail;
import com.github.laxika.magicalvibes.cards.d.DrownerInitiate;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunedHalo.class, BurnTrail.class, BriarberryCohort.class,
        WheelOfSunAndMoon.class, DrownerInitiate.class})
class RunedHaloTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Runed Halo awaits a card name choice and records it on the permanent")
    void resolvingChoosesCardName() {
        harness.setHand(player1, List.of(new RunedHalo()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Burn Trail");

        Permanent halo = findPermanent(player1, "Runed Halo");
        assertThat(halo.getChosenName()).isEqualTo("Burn Trail");
    }

    @Test
    @DisplayName("A spell with the chosen name can't target the protected player")
    void chosenNameSpellCannotTargetPlayer() {
        addReadyRunedHalo(player1, "Burn Trail");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BurnTrail()));
        harness.addMana(player2, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection is player-scoped: the named spell can still target a permanent the player controls")
    void chosenNameSpellCanStillTargetPlayersPermanent() {
        addReadyRunedHalo(player1, "Burn Trail");
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BurnTrail()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player2, 0, cohort.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A spell with a different name can still target the protected player")
    void differentNameSpellCanTargetPlayer() {
        addReadyRunedHalo(player1, "Lightning Bolt");
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BurnTrail()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An Aura with the chosen name can't enchant the protected player")
    void chosenNameAuraCannotEnchantPlayer() {
        addReadyRunedHalo(player1, "Wheel of Sun and Moon");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WheelOfSunAndMoon()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection ends when Runed Halo loses all abilities")
    void protectionEndsWhenHaloLosesAbilities() {
        Permanent halo = addReadyRunedHalo(player1, "Burn Trail");
        halo.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BurnTrail()));
        harness.addMana(player2, ManaColor.RED, 4);

        assertThatCode(() -> harness.castAndResolveSorcery(player2, 0, player1.getId()))
                .doesNotThrowAnyException();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An ability from a source with the chosen name can't target the protected player")
    void chosenNameAbilityCannotTargetPlayer() {
        addReadyRunedHalo(player1, "Drowner Initiate");
        harness.addToBattlefield(player2, new DrownerInitiate());
        harness.setLibrary(player1, List.of(new BriarberryCohort(), new BriarberryCohort()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BriarberryCohort()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player2, 0);
        harness.handleMayAbilityChosen(player2, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A creature with the chosen name deals no combat damage to the protected player")
    void chosenNameAttackerDealsNoCombatDamage() {
        addReadyRunedHalo(player1, "Briarberry Cohort");
        harness.setLife(player1, 20);

        Permanent attacker = addCreatureReady(player2, new BriarberryCohort());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature with a different name still deals combat damage to the protected player")
    void differentNameAttackerDealsCombatDamage() {
        addReadyRunedHalo(player1, "Burn Trail");
        harness.setLife(player1, 20);

        Permanent attacker = addCreatureReady(player2, new BriarberryCohort());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A face-down attacker has no name and its damage is not prevented")
    void faceDownAttackerDoesNotMatchChosenName() {
        addReadyRunedHalo(player1, "Briarberry Cohort");
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new BriarberryCohort());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A name that is not a real card name cannot be chosen")
    void rejectsNonexistentCardName() {
        harness.setHand(player1, List.of(new RunedHalo()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Not a real Magic card name"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("An already attached Aura with the chosen name is put into its owner's graveyard")
    void chosenNameAuraAlreadyAttachedBecomesIllegal() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new WheelOfSunAndMoon()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castEnchantment(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Wheel of Sun and Moon");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RunedHalo()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Wheel of Sun and Moon");

        harness.assertNotOnBattlefield(player2, "Wheel of Sun and Moon");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof WheelOfSunAndMoon);
    }

    @Test
    @DisplayName("A named spell loses its player target if protection is gained before resolution")
    void protectionGainedBeforeResolutionMakesTargetIllegal() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BurnTrail()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castSorcery(player2, 0, player1.getId());

        addReadyRunedHalo(player1, "Burn Trail");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection also forbids the controller's own spell from targeting them")
    void ownNamedSpellCannotTargetProtectedController() {
        addReadyRunedHalo(player1, "Burn Trail");
        harness.setHand(player1, List.of(new BurnTrail()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private Permanent addReadyRunedHalo(Player player, String chosenName) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RunedHalo());
        perm.setChosenName(chosenName);
        return perm;
    }
}
