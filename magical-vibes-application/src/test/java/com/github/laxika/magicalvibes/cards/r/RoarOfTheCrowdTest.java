package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoarOfTheCrowdTest extends BaseCardTest {

    private void handSpellAndMana() {
        harness.setHand(player1, List.of(new RoarOfTheCrowd()));
        harness.addMana(player1, ManaColor.RED, 4);
    }

    @Test
    @DisplayName("Deals damage to target player equal to the number of permanents of the chosen type you control")
    void dealsDamageToPlayerEqualToChosenTypeCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Lethal damage from the chosen-type count destroys a target creature")
    void lethalCountKillsTargetCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        handSpellAndMana();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Choosing a type you control none of deals no damage")
    void chosenTypeYouControlNoneDealsZero() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.setLife(player2, 20);
        handSpellAndMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
