package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuestingCosplayer.class, GrizzlyBears.class, Fog.class})
class QuestingCosplayerTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Questing Role attached to any target creature and grants Questing Beast's abilities")
    void createsQuestingRoleAndGrantsAbilities() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        attachQuestingRole(target);

        Permanent role = findPermanent(player1, "Questing Role");
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.getGrantedEffects(gd, target))
                .anyMatch(GrantTriggeredAbilityEffect.class::isInstance);
    }

    @Test
    @DisplayName("the enchanted creature cannot be blocked by creatures with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachQuestingRole(target);
        target.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("grants unpreventable combat damage and Questing Beast's combat-damage trigger")
    void combatDamageCannotBePreventedAndDamagesDefendingCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        attachQuestingRole(target);

        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        target.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(defendingCreature);
    }

    private void attachQuestingRole(Permanent target) {
        harness.setHand(player1, List.of(new QuestingCosplayer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
