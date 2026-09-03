package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.CallToArms;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Infuse.class, BalduvianBears.class, SnowCoveredForest.class, ZuranOrb.class, CallToArms.class})
class InfuseTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target creature and schedules a draw at the next upkeep")
    void untapsCreatureAndSchedulesDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        creature.tap();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isFalse();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can untap a target land")
    void untapsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        land.tap();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, land.getId());

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap a target artifact")
    void untapsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        artifact.tap();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target an already untapped permanent")
    void canTargetUntappedPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Fizzles without scheduling a draw when the target leaves before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CallToArms());

        harness.setHand(player1, List.of(new Infuse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }
}
