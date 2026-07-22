package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlteredEgoTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature and enters with X additional +1/+1 counters")
    void copiesWithXCounters() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlteredEgo()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 5); // {2} + X=3

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent copy = findAlteredEgoCopy(player1);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(copy.getCard().getPower()).isEqualTo(2);
        assertThat(copy.getCard().getToughness()).isEqualTo(2);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(copy.getEffectivePower()).isEqualTo(5);
        assertThat(copy.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("X=0 copy enters without additional counters")
    void copiesWithXZero() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlteredEgo()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent copy = findAlteredEgoCopy(player1);
        assertThat(copy).isNotNull();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.getEffectivePower()).isEqualTo(2);
        assertThat(copy.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining to copy enters as 0/0 with no counters and dies")
    void declinesWithoutCounters() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlteredEgo()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Altered Ego"));
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Altered Ego"));
    }

    @Test
    @DisplayName("This spell can't be countered")
    void cantBeCountered() {
        AlteredEgo ego = new AlteredEgo();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(ego));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, ego.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Altered Ego"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    private Permanent findAlteredEgoCopy(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Altered Ego"))
                .findFirst().orElse(null);
    }
}
