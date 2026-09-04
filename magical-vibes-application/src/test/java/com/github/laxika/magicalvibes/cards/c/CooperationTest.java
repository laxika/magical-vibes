package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdarkarUnicorn;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cooperation.class, AdarkarUnicorn.class, ZuranOrb.class})
class CooperationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has banding")
    void enchantedCreatureHasBanding() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new AdarkarUnicorn());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Cooperation());
        aura.setAttachedTo(unicorn.getId());

        assertThat(gqs.hasKeyword(gd, unicorn, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Creature loses banding when Cooperation is removed")
    void bandingStopsWhenRemoved() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new AdarkarUnicorn());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Cooperation());
        aura.setAttachedTo(unicorn.getId());

        assertThat(gqs.hasKeyword(gd, unicorn, Keyword.BANDING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, unicorn, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Enters attached to the target creature and grants banding")
    void entersAttachedToTargetCreature() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new AdarkarUnicorn());
        harness.setHand(player1, List.of(new Cooperation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, unicorn.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Cooperation");
        assertThat(aura.getAttachedTo()).isEqualTo(unicorn.getId());
        assertThat(gqs.hasKeyword(gd, unicorn, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        harness.setHand(player1, List.of(new Cooperation()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Granted banding lets the defending player assign combat damage")
    void grantedBandingChangesCombatDamageAssignment() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new AdarkarUnicorn());
        Permanent bandingBlocker = addCreatureReady(player2, new AdarkarUnicorn());
        Permanent plainBlocker = addCreatureReady(player2, new AdarkarUnicorn());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Cooperation());
        aura.setAttachedTo(bandingBlocker.getId());

        attacker.setAttacking(true);
        bandingBlocker.setBlocking(true);
        bandingBlocker.addBlockingTarget(0);
        plainBlocker.setBlocking(true);
        plainBlocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());

        harness.handleCombatDamageAssigned(player2, 0, Map.of(plainBlocker.getId(), 2));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(bandingBlocker)
                .doesNotContain(plainBlocker);
    }
}
