package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuanYus1000LiMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys tapped creatures on both sides")
    void destroysTappedCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        bears.tap();
        Permanent elves = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();
        elves.tap();

        harness.setHand(player1, List.of(new GuanYus1000LiMarch()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Untapped creatures are not destroyed")
    void untappedCreaturesSurvive() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GuanYus1000LiMarch()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Indestructible tapped creature survives")
    void indestructibleTappedCreatureSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        bears.tap();
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new GuanYus1000LiMarch()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
