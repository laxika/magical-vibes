package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.y.YoungBlueDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        resolveAllTriggers();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        resolveAllTriggers();

        assertThat(gd.findExiledCard(hex.getId())).isNotNull();
        assertThat(gd.exiledCardsWithFetchCounters).contains(hex.getId());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, dragon.getId());
        harness.passBothPriorities();

        Permanent returnedHex = findPermanent(player1, "Hex, Kellan's Companion");
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

    @Test
    void repeatedAdventuresWhileExiledAccumulatePerpetualBoosts() {
        HexKellansCompanion hex = new HexKellansCompanion();
        harness.enterBattlefieldAndReturn(player1, hex);
        castAndResolveAdventure(player1);
        YoungBlueDragon secondDragon = castAndResolveAdventure(player1);

        assertThat(gd.findExiledCard(hex.getId())).isNotNull();
        assertThat(gd.exiledCardsWithFetchCounters).contains(hex.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, secondDragon.getId());
        resolveAllTriggers();

        Permanent returnedHex = findPermanent(player1, "Hex, Kellan's Companion");
        assertThat(gqs.getEffectivePower(gd, returnedHex)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedHex)).isEqualTo(3);
        assertThat(returnedHex.isTapped()).isFalse();
        assertThat(gd.exiledCardsWithFetchCounters).doesNotContain(hex.getId());
    }

    @Test
    void exiledHexWithoutFetchCounterStillGrowsButDoesNotReturn() {
        HexKellansCompanion hex = new HexKellansCompanion();
        harness.setExile(player1, List.of(hex));
        YoungBlueDragon dragon = castAndResolveAdventure(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, dragon.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(hex.getId())).isNotNull();
        assertThat(gd.exiledCardsWithFetchCounters).doesNotContain(hex.getId());
        harness.assertNotOnBattlefield(player1, "Hex, Kellan's Companion");

        gd.removeFromExile(hex.getId());
        Permanent returnedHex = harness.enterBattlefieldAndReturn(player1, hex);
        assertThat(gqs.getEffectivePower(gd, returnedHex)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returnedHex)).isEqualTo(2);
    }

    @Test
    void castingCreatureFromHandDoesNotReturnHexWithFetchCounter() {
        HexKellansCompanion hex = new HexKellansCompanion();
        harness.enterBattlefieldAndReturn(player1, hex);
        castAndResolveAdventure(player1);
        harness.setHand(player1, List.of(new YoungBlueDragon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(hex.getId())).isNotNull();
        assertThat(gd.exiledCardsWithFetchCounters).contains(hex.getId());
        harness.assertNotOnBattlefield(player1, "Hex, Kellan's Companion");
    }

    @Test
    void adventuresDoNotBoostHexInGraveyard() {
        HexKellansCompanion hex = new HexKellansCompanion();
        harness.setGraveyard(player1, List.of(hex));
        castAndResolveAdventure(player1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hex);
        harness.setGraveyard(player1, List.of());
        Permanent returnedHex = harness.enterBattlefieldAndReturn(player1, hex);
        assertThat(gqs.getEffectivePower(gd, returnedHex)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returnedHex)).isEqualTo(1);
    }

    @Test
    void opponentsAdventureDoesNotBoostOrExileHex() {
        HexKellansCompanion hex = new HexKellansCompanion();
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, hex);
        harness.forceActivePlayer(player2);
        castAndResolveAdventure(player2);

        harness.assertOnBattlefield(player1, "Hex, Kellan's Companion");
        assertThat(gd.findExiledCard(hex.getId())).isNull();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(1);
    }

    private YoungBlueDragon castAndResolveAdventure(Player player) {
        YoungBlueDragon dragon = new YoungBlueDragon();
        harness.setHand(player, List.of(dragon));
        harness.setLibrary(player, List.of(new Island(), new Island()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castAdventure(player, 0, List.of());
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        resolveAllTriggers();
        return dragon;
    }
}
