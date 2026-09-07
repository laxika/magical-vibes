package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RitualOfSteel.class, IronTuskElephant.class, Plains.class})
class RitualOfSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +0/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new IronTuskElephant());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RitualOfSteel());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off when the Aura leaves the battlefield")
    void boostGoesAwayWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new IronTuskElephant());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RitualOfSteel());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent elephant = addCreatureReady(player2, new IronTuskElephant());
        harness.setHand(player1, List.of(new RitualOfSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, elephant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(5);
    }

    @Test
    @DisplayName("Entering the battlefield schedules a draw at the next upkeep")
    void schedulesDrawOnEnter() {
        Permanent bears = addCreatureReady(player1, new IronTuskElephant());

        harness.setHand(player1, List.of(new RitualOfSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve the Aura
        harness.passBothPriorities(); // resolve the enter-the-battlefield trigger

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep")
    void drawResolvesAtNextUpkeep() {
        Permanent bears = addCreatureReady(player1, new IronTuskElephant());

        harness.setHand(player1, List.of(new RitualOfSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve the Aura
        harness.passBothPriorities(); // resolve the enter-the-battlefield trigger

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
    @DisplayName("The delayed draw still resolves if the Aura leaves first")
    void drawResolvesAfterAuraLeaves() {
        Permanent elephant = addCreatureReady(player1, new IronTuskElephant());
        harness.setHand(player1, List.of(new RitualOfSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, elephant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Ritual of Steel"));
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
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new RitualOfSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
