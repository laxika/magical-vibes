package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetessanBattlePriest.class, Shock.class, GiantGrowth.class})
class SetessanBattlePriestTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Setessan Battle Priest gains 2 life")
    void castingSpellThatTargetsPriestGainsLife() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID priestId = harness.getPermanentId(player1, "Setessan Battle Priest");
        harness.castAndResolveInstant(player1, 0, priestId);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Setessan Battle Priest")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's spell that targets Setessan Battle Priest does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID priestId = harness.getPermanentId(player1, "Setessan Battle Priest");
        harness.castAndResolveInstant(player2, 0, priestId);

        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each separate spell targeting the Priest triggers heroic")
    void separateSpellsEachGainLife() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        UUID priestId = harness.getPermanentId(player1, "Setessan Battle Priest");

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, priestId);
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Targeting another Priest does not trigger your Priest")
    void targetingAnotherCreatureDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.addToBattlefield(player2, new SetessanBattlePriest());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Setessan Battle Priest"));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Heroic resolves even if the Priest dies before its trigger resolves")
    void heroicResolvesAfterSourceDies() {
        harness.addToBattlefield(player1, new SetessanBattlePriest());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        UUID priestId = harness.getPermanentId(player1, "Setessan Battle Priest");

        harness.castAndResolveInstant(player1, 0, priestId);
        harness.assertLife(player1, 22);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, priestId);
        harness.assertLife(player1, 22);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, priestId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Setessan Battle Priest");
        harness.assertLife(player1, 22);

        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
