package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.b.BigScore;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZiatorasEnvoy.class, AirElemental.class, ColossalDreadmaw.class, Forest.class, BigScore.class})
class ZiatorasEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets the controller cast a spell with mana value equal to the damage")
    void castsSpellWithManaValueEqualToDamage() {
        AirElemental airElemental = new AirElemental();
        harness.setLibrary(player1, List.of(airElemental));
        addAttackingEnvoy();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack.getLast().getCard()).isSameAs(airElemental);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(airElemental);
    }

    @Test
    @DisplayName("A spell above the damage limit goes directly to hand")
    void putsTooExpensiveSpellIntoHand() {
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(dreadmaw));
        addAttackingEnvoy();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(dreadmaw);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(dreadmaw);
    }

    @Test
    @DisplayName("Declining the top-card choice puts the card into hand")
    void decliningPutsCardIntoHand() {
        AirElemental airElemental = new AirElemental();
        harness.setLibrary(player1, List.of(airElemental));
        addAttackingEnvoy();

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(airElemental);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(airElemental);
    }

    @Test
    @DisplayName("Combat damage lets the controller play an eligible land from the top")
    void playsLandFromTop() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addAttackingEnvoy();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("An empty library leaves the combat trigger with nothing to play or put into hand")
    void emptyLibraryDoesNothing() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        addAttackingEnvoy();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land goes to hand when the land play allowance is exhausted")
    void exhaustedLandAllowancePutsLandIntoHand() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        addAttackingEnvoy();

        resolveCombatAndTrigger();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining to play a land puts it into hand without spending a land play")
    void decliningLandPutsItIntoHand() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addAttackingEnvoy();

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("A spell cannot be cast for free when its mandatory discard cost cannot be paid")
    void unpayableAdditionalCostPutsSpellIntoHand() {
        BigScore bigScore = new BigScore();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(bigScore));
        addAttackingEnvoy();

        resolveCombatAndTrigger();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Big Score");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bigScore);
    }

    @Test
    @DisplayName("Blitz grants haste and sacrifices the Envoy at the next end step, drawing a card")
    void blitzGrantsHasteSacrificesAndDraws() {
        harness.setHand(player1, List.of(new ZiatorasEnvoy()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent envoy = findPermanent(player1, "Ziatora's Envoy");
        assertThat(gqs.hasKeyword(gd, envoy, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ziatora's Envoy");
        harness.assertInGraveyard(player1, "Ziatora's Envoy");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("The normal casting cost does not grant haste or cause an end-step sacrifice")
    void normalCastDoesNotApplyBlitz() {
        harness.castFromHand(player1, new ZiatorasEnvoy(), "{1}{B}{R}{G}");
        harness.passBothPriorities();

        Permanent envoy = findPermanent(player1, "Ziatora's Envoy");
        assertThat(gqs.hasKeyword(gd, envoy, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ziatora's Envoy");
        harness.assertNotInGraveyard(player1, "Ziatora's Envoy");
    }

    private Permanent addAttackingEnvoy() {
        Permanent envoy = addCreatureReady(player1, new ZiatorasEnvoy());
        envoy.setAttacking(true);
        return envoy;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
