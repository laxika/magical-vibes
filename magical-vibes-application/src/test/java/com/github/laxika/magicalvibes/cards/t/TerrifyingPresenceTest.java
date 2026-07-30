package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerrifyingPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature can still deal combat damage")
    void targetCreatureStillDealsCombatDamage() {
        Permanent target = addCreature(player1);
        castTerrifyingPresence(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isFalse();
    }

    @Test
    @DisplayName("Every other creature is prevented from dealing combat damage")
    void otherCreaturesPreventedFromDealingCombatDamage() {
        Permanent target = addCreature(player1);
        Permanent otherOwn = addCreature(player1);
        Permanent opponent = addCreature(player2);

        castTerrifyingPresence(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, otherOwn, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, opponent, true)).isTrue();
    }

    @Test
    @DisplayName("Non-combat damage from other creatures is unaffected")
    void nonCombatDamageIsUnaffected() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player2);

        castTerrifyingPresence(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, other, false)).isFalse();
    }

    @Test
    @DisplayName("Prevention wears off at end of turn")
    void preventionWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player1);
        Permanent other = addCreature(player2);
        castTerrifyingPresence(target);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.isPreventedFromDealingDamage(gd, other, true)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new TerrifyingPresence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = addCreature(player1);
        castTerrifyingPresence(target);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Terrifying Presence");
    }

    private void castTerrifyingPresence(Permanent target) {
        harness.setHand(player1, List.of(new TerrifyingPresence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
