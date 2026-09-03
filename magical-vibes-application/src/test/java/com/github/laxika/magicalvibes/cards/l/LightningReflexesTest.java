package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningReflexes.class, IronTuskElephant.class, Island.class})
class LightningReflexesTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0 and first strike")
    void enchantedCreatureGetsBoostAndFirstStrike() {
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, elephant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elephant, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and first strike are lost when the Aura leaves the battlefield")
    void boostLostWhenAuraRemoved() {
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, elephant.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Lightning Reflexes");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elephant, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, elephant.getId());
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Lightning Reflexes");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, elephant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lightning Reflexes");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Lightning Reflexes");
        harness.assertInGraveyard(player1, "Lightning Reflexes");
    }

    @Test
    @DisplayName("Cast during an opponent's turn, its controller sacrifices it at cleanup")
    void castDuringOpponentsTurnIsSacrificedByItsControllerAtCleanup() {
        Permanent opposingElephant = addCreatureReady(player2, new IronTuskElephant());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, opposingElephant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingElephant)).isEqualTo(4);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Lightning Reflexes");
        harness.assertInGraveyard(player1, "Lightning Reflexes");
        assertThat(gqs.getEffectivePower(gd, opposingElephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new LightningReflexes()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent nonCreature = findPermanent(player1, "Island");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
