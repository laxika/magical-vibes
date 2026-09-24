package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlagueWind;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AishaOfSparksAndSmoke.class, Divination.class, GrizzlyBears.class, PlagueWind.class,
        Shock.class})
class AishaOfSparksAndSmokeTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Aisha +1/+1 for the turn")
    void prowessBoostsAisha() {
        Permanent aisha = addAisha();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aisha)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aisha)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aisha)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aisha)).isEqualTo(2);
    }

    @Test
    @DisplayName("The hybrid ability grants first strike until end of turn")
    void grantsFirstStrike() {
        Permanent aisha = addAisha();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aisha, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aisha, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage offers a sorcery at most equal to the damage for free")
    void combatDamageFreeCastsAffordableSorcery() {
        Permanent aisha = addAisha();
        aisha.setAttacking(true);
        Divination sorcery = new Divination();
        harness.setHand(player1, List.of(sorcery));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(sorcery.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage does not offer instants, creatures, or expensive sorceries")
    void combatDamageFiltersHandByCardTypeAndDamage() {
        Permanent aisha = addAisha();
        aisha.setAttacking(true);
        Shock instant = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        PlagueWind expensiveSorcery = new PlagueWind();
        harness.setHand(player1, List.of(instant, creature, expensiveSorcery));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant, creature, expensiveSorcery);
    }

    private Permanent addAisha() {
        return addCreatureReady(player1, new AishaOfSparksAndSmoke());
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
