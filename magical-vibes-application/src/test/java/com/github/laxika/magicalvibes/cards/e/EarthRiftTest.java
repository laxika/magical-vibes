package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthRift.class, AngelicWall.class, Mountain.class})
class EarthRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast destroys target land")
    void normalCastDestroysTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new EarthRift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Mountain"));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Earth Rift");
    }

    @Test
    @DisplayName("Flashback destroys target land and exiles Earth Rift")
    void flashbackDestroysTargetLandAndExilesSpell() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setGraveyard(player1, List.of(new EarthRift()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, harness.getPermanentId(player2, "Mountain"));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertNotInGraveyard(player1, "Earth Rift");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Earth Rift"));
    }

    @Test
    @DisplayName("Flashback exiles Earth Rift when its target is gone before resolution")
    void flashbackExilesOnFizzle() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setGraveyard(player1, List.of(new EarthRift()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, harness.getPermanentId(player2, "Mountain"));
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Earth Rift");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Earth Rift"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AngelicWall());
        harness.setHand(player1, List.of(new EarthRift()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Angelic Wall")))
                .isInstanceOf(IllegalStateException.class);
    }
}
