package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TriumphOfTheHordesTest extends BaseCardTest {

    @Test
    @DisplayName("Triumph of the Hordes has correct effects")
    void hasCorrectEffects() {
        TriumphOfTheHordes card = new TriumphOfTheHordes();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(BoostAllOwnCreaturesEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GrantKeywordEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(GrantKeywordEffect.class);

        GrantKeywordEffect trampleGrant = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(trampleGrant.keywords()).containsExactly(Keyword.TRAMPLE);
        assertThat(trampleGrant.scope()).isEqualTo(GrantScope.OWN_CREATURES);

        GrantKeywordEffect infectGrant = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(2);
        assertThat(infectGrant.keywords()).containsExactly(Keyword.INFECT);
        assertThat(infectGrant.scope()).isEqualTo(GrantScope.OWN_CREATURES);
    }

    @Test
    @DisplayName("Resolving gives own creatures +1/+1, trample, and infect")
    void resolvesAndBuffsOwnCreatures() {
        Permanent p1a = addReadyCreature(player1, new GrizzlyBears());
        Permanent p1b = addReadyCreature(player1, new GrizzlyBears());
        Permanent p2 = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(p1a.getEffectivePower()).isEqualTo(3);
        assertThat(p1a.getEffectiveToughness()).isEqualTo(3);
        assertThat(p1b.getEffectivePower()).isEqualTo(3);
        assertThat(p1b.getEffectiveToughness()).isEqualTo(3);
        assertThat(p1a.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(p1b.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(p1a.hasKeyword(Keyword.INFECT)).isTrue();
        assertThat(p1b.hasKeyword(Keyword.INFECT)).isTrue();

        assertThat(p2.getEffectivePower()).isEqualTo(2);
        assertThat(p2.getEffectiveToughness()).isEqualTo(2);
        assertThat(p2.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(p2.hasKeyword(Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(creature.hasKeyword(Keyword.INFECT)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(creature.hasKeyword(Keyword.INFECT)).isFalse();
    }

    @Test
    @DisplayName("Infect creature deals poison counters to defending player via trample")
    void infectDealsPoisonCountersViaTrample() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 3/3 trample+infect blocked by 2/2 → assign lethal to blocker, excess to player as poison
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        // Infect damage to player = poison counters, not life loss
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting puts it on stack as sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TriumphOfTheHordes()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Triumph of the Hordes");
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
