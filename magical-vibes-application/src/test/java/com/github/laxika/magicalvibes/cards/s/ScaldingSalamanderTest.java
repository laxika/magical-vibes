package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScaldingSalamander.class, SabertoothWyvern.class})
class ScaldingSalamanderTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the attack trigger damages defending creatures without flying")
    void damagesDefendingCreaturesWithoutFlying() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defendingSalamander = addCreatureReady(player2, new ScaldingSalamander());
        Permanent defendingSalamander2 = addCreatureReady(player2, new ScaldingSalamander());
        Permanent defendingWyvern = addCreatureReady(player2, new SabertoothWyvern());
        Permanent ownSalamander = addCreatureReady(player1, new ScaldingSalamander());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(defendingSalamander.getMarkedDamage()).isEqualTo(1);
        assertThat(defendingSalamander2.getMarkedDamage()).isEqualTo(1);
        assertThat(defendingWyvern.getMarkedDamage()).isZero();
        assertThat(ownSalamander.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Accepting the attack trigger with no matching creatures deals no damage")
    void acceptingAttackTriggerWithNoMatchingCreaturesDealsNoDamage() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defendingWyvern = addCreatureReady(player2, new SabertoothWyvern());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(defendingWyvern.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger deals no damage")
    void decliningAttackTriggerDealsNoDamage() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defendingSalamander = addCreatureReady(player2, new ScaldingSalamander());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingSalamander.getMarkedDamage()).isZero();
    }
}
