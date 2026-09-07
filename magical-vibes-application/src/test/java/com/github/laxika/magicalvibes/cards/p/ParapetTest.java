package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Parapet.class, Warthog.class, Tremor.class})
class ParapetTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +0/+1")
    void buffsOwnCreatures() {
        harness.addToBattlefield(player1, new Parapet());
        Permanent warthog = addCreatureReady(player1, new Warthog());

        assertThat(gqs.getEffectivePower(gd, warthog)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warthog)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentsCreatures() {
        harness.addToBattlefield(player1, new Parapet());
        Permanent warthog = addCreatureReady(player2, new Warthog());

        assertThat(gqs.getEffectivePower(gd, warthog)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warthog)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost is lost when Parapet leaves the battlefield")
    void boostLostWhenRemoved() {
        Permanent parapet = new Permanent(new Parapet());
        gd.playerBattlefields.get(player1.getId()).add(parapet);
        Permanent warthog = addCreatureReady(player1, new Warthog());

        assertThat(gqs.getEffectiveToughness(gd, warthog)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(parapet);

        assertThat(gqs.getEffectiveToughness(gd, warthog)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        harness.castFromHand(player1, new Parapet(), "{1}{W}");
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Parapet");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new Parapet(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Parapet");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Parapet");
        harness.assertInGraveyard(player1, "Parapet");
    }

    @Test
    @DisplayName("Cast during a main phase while another spell is on the stack, it is sacrificed at cleanup")
    void castWithAnotherSpellOnStackIsSacrificedAtCleanup() {
        harness.castFromHand(player1, new Tremor(), "{R}");
        harness.castFromHand(player1, new Parapet(), "{1}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Parapet");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Parapet");
        harness.assertInGraveyard(player1, "Parapet");
    }

    @Test
    @DisplayName("Can be cast by a nonactive player at instant speed")
    void nonactivePlayerCanCastAtInstantSpeed() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new Parapet(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Parapet");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Parapet");
        harness.assertInGraveyard(player1, "Parapet");
    }

    @Test
    @DisplayName("Cast from exile with its own flash permission is sacrificed at cleanup")
    void castFromExileAtInstantSpeedIsSacrificedAtCleanup() {
        Parapet parapet = new Parapet();
        gd.addToExile(player1.getId(), parapet);
        gd.exilePlayPermissions.put(parapet.getId(), player1.getId());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castFromExile(player1, parapet.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Parapet");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Parapet");
        harness.assertInGraveyard(player1, "Parapet");
    }
}
