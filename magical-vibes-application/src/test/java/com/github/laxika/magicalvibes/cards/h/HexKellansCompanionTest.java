package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.y.YoungBlueDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HexKellansCompanion.class, YoungBlueDragon.class, Island.class})
class HexKellansCompanionTest extends BaseCardTest {

    @Test
    void adventureExilesHexWithFetchCounterAndAdventureCreatureCastReturnsItBuffed() {
        HexKellansCompanion hex = new HexKellansCompanion();
        YoungBlueDragon dragon = new YoungBlueDragon();
        harness.enterBattlefieldAndReturn(player1, hex);
        harness.setHand(player1, List.of(dragon));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(hex.getId())).isNotNull();
        assertThat(gd.exiledCardsWithFetchCounters).contains(hex.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, dragon.getId());
        harness.passBothPriorities();

        Permanent returnedHex = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == hex)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, returnedHex)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returnedHex)).isEqualTo(2);
        assertThat(gd.exiledCardsWithFetchCounters).doesNotContain(hex.getId());
        assertThat(gd.findExiledCard(hex.getId())).isNull();
    }

    @Test
    void ordinaryCastOfAnAdventureCardDoesNotTriggerHex() {
        HexKellansCompanion hex = new HexKellansCompanion();
        YoungBlueDragon dragon = new YoungBlueDragon();
        harness.enterBattlefieldAndReturn(player1, hex);
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard() == hex)).isTrue();
        assertThat(gd.findExiledCard(hex.getId())).isNull();
        assertThat(gd.exiledCardsWithFetchCounters).doesNotContain(hex.getId());
    }
}
