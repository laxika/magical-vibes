package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThrabenSentryTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        ThrabenSentry card = new ThrabenSentry();

        // Front face: ON_ALLY_CREATURE_DIES may-transform trigger
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(TransformSelfEffect.class);

        // Back face exists and is vanilla
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("ThrabenMilitia");
    }

    // ===== Transform when allied creature dies (accept) =====

    @Test
    @DisplayName("Transforms when another creature you control dies and you choose yes")
    void transformsWhenAllyCreatureDiesAccept() {
        harness.addToBattlefield(player1, new ThrabenSentry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent sentry = findPermanent(player1, "Thraben Sentry");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        // Player1 has two creatures — must choose which to sacrifice
        harness.passBothPriorities(); // resolve Cruel Edict → prompted to choose

        // Sacrifice the Grizzly Bears, keeping Thraben Sentry
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        // ON_ALLY_CREATURE_DIES trigger goes on stack → resolve → MayEffect prompts
        harness.passBothPriorities();

        // Accept the may transform
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentry.isTransformed()).isTrue();
        assertThat(sentry.getCard().getName()).isEqualTo("Thraben Militia");
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(4);
    }

    // ===== Does not transform when declined =====

    @Test
    @DisplayName("Does not transform when you choose no")
    void doesNotTransformWhenDeclined() {
        harness.addToBattlefield(player1, new ThrabenSentry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent sentry = findPermanent(player1, "Thraben Sentry");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sentry.isTransformed()).isFalse();
        assertThat(sentry.getCard().getName()).isEqualTo("Thraben Sentry");
    }

    // ===== Does not trigger when opponent's creature dies =====

    @Test
    @DisplayName("Does not trigger when opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new ThrabenSentry());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → opponent's creature dies

        GameData gd = harness.getGameData();
        // No trigger on the stack — ON_ALLY_CREATURE_DIES only fires for controller's creatures
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
    }

    // ===== Does not trigger when Thraben Sentry itself dies =====

    @Test
    @DisplayName("Does not trigger when Thraben Sentry itself dies (only 'another creature')")
    void doesNotTriggerWhenSelfDies() {
        harness.addToBattlefield(player1, new ThrabenSentry());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → Thraben Sentry dies

        GameData gd = harness.getGameData();
        // No trigger — Sentry is no longer on the battlefield
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thraben Sentry"));
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
