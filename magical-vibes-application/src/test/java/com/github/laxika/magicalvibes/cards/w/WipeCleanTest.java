package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvenLiberator;
import com.github.laxika.magicalvibes.cards.f.FacesOfThePast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WipeClean.class, FacesOfThePast.class, AvenLiberator.class})
class WipeCleanTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target enchantment")
    void exilesTargetEnchantment() {
        harness.addToBattlefield(player2, new FacesOfThePast());
        harness.setHand(player1, List.of(new WipeClean()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Faces of the Past");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Faces of the Past");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Faces of the Past"));
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AvenLiberator());
        harness.setHand(player1, List.of(new WipeClean()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Aven Liberator");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new WipeClean()));
        harness.setLibrary(player1, List.of(new AvenLiberator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wipe Clean");
        harness.assertInHand(player1, "Aven Liberator");
    }
}
