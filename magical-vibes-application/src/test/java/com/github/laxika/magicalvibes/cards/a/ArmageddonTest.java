package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArmageddonTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all lands controlled by both players")
    void destroysAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Armageddon()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getType().name().equals("LAND"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getType().name().equals("LAND"));
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Does not destroy non-land permanents")
    void doesNotDestroyNonLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Armageddon()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
