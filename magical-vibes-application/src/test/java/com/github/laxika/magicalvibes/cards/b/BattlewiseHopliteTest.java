package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BattlewiseHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Battlewise Hoplite puts a counter on it and scries 1")
    void castingSpellThatTargetsHopliteTriggersHeroic() {
        harness.addToBattlefield(player1, new BattlewiseHoplite());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID hopliteId = harness.getPermanentId(player1, "Battlewise Hoplite");
        harness.castInstant(player1, 0, hopliteId);
        harness.passBothPriorities();

        Permanent hoplite = findPermanent(player1, "Battlewise Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Battlewise Hoplite")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new BattlewiseHoplite());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent hoplite = findPermanent(player1, "Battlewise Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's spell that targets Battlewise Hoplite does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new BattlewiseHoplite());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID hopliteId = harness.getPermanentId(player1, "Battlewise Hoplite");
        harness.castInstant(player2, 0, hopliteId);
        harness.passBothPriorities();

        Permanent hoplite = findPermanent(player1, "Battlewise Hoplite");
        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
