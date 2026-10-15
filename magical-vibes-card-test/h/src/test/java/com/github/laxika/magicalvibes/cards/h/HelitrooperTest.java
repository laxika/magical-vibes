package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LionSash;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Helitrooper.class, GrizzlyBears.class, LeoninScimitar.class, LionSash.class})
class HelitrooperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets another attacking creature and grants it flying")
    void attackingGrantsFlyingToAnotherAttackingCreature() {
        Permanent trooper = addCreatureReady(player1, new Helitrooper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        assertThat(trooper.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void grantedFlyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new Helitrooper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Equip abilities targeting Helitrooper cost two less")
    void equipmentAbilitiesTargetingHelitrooperAreReduced() {
        Permanent trooper = addCreatureReady(player1, new Helitrooper());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null,
                trooper.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(trooper.getId());
    }

    @Test
    @DisplayName("Cannot target Helitrooper itself or a nonattacking creature")
    void attackTriggerRequiresAnotherAttackingCreature() {
        Permanent trooper = addCreatureReady(player1, new Helitrooper());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, trooper.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();
    }

    @Test
    void equipCostIsNotReducedForAnotherCreature() {
        addCreatureReady(player1, new Helitrooper());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null, other.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(scimitar.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCostIsNotReduced() {
        Permanent trooper = addCreatureReady(player1, new Helitrooper());
        Permanent sash = harness.addToBattlefieldAndReturn(player1, new LionSash());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(sash), 1, null, trooper.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(sash.getAttachedTo()).isNull();
    }
}
