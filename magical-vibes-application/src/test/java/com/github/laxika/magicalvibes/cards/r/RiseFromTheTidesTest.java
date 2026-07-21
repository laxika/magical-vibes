package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiseFromTheTidesTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one tapped 2/2 Zombie per instant/sorcery in graveyard")
    void createsTappedZombiesPerInstantOrSorceryInGraveyard() {
        harness.setGraveyard(player1, List.of(new Opt(), new LightningBolt(), new Opt()));
        harness.setHand(player1, List.of(new RiseFromTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .toList();

        assertThat(zombies).hasSize(3);
        for (Permanent zombie : zombies) {
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
            assertThat(zombie.isTapped()).isTrue();
        }
    }

    @Test
    @DisplayName("No tokens when graveyard has no instant or sorcery cards")
    void noTokensWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new RiseFromTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        long zombieCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .count();

        assertThat(zombieCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Creature cards in graveyard are not counted")
    void creatureCardsNotCounted() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Opt()));
        harness.setHand(player1, List.of(new RiseFromTheTides()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        long zombieCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .count();

        assertThat(zombieCount).isEqualTo(1);
    }
}
