package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WildfireTest extends BaseCardTest {

    private void addLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Mountain());
            harness.addToBattlefield(player2, new Forest());
        }
    }

    private void castWildfire() {
        harness.setHand(player1, List.of(new Wildfire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private long landCount(com.github.laxika.magicalvibes.model.Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mountain")
                        || p.getCard().getName().equals("Forest"))
                .count();
    }

    @Test
    @DisplayName("Each player with exactly four lands sacrifices all of them")
    void eachPlayerSacrificesFourLands() {
        addLands(4);

        castWildfire();

        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isZero();
    }

    @Test
    @DisplayName("A player with fewer than four lands sacrifices all of them")
    void playerWithFewerLandsSacrificesAll() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());

        castWildfire();

        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isZero();
    }

    @Test
    @DisplayName("A player with more than four lands chooses which four to sacrifice")
    void playerWithMoreLandsChooses() {
        // player1 has exactly 4 (auto), player2 has 5 (must choose 4)
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.setHand(player1, List.of(new Wildfire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        List<UUID> toSacrifice = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .limit(4)
                .map(Permanent::getId)
                .toList();
        harness.handleMultiplePermanentsChosen(player2, toSacrifice);

        assertThat(landCount(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 4 damage to each creature, killing small creatures and sparing large ones")
    void dealsFourDamageToEachCreature() {
        addLands(4);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AvatarOfMight());

        castWildfire();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Avatar of Might"));
    }
}
