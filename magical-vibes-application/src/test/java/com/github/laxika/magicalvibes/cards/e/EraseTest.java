package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicCurator;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.cards.l.Levitation;
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

@CardUsed({Erase.class, Levitation.class, GrimMonolith.class, AngelicCurator.class})
class EraseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving exiles target enchantment instead of destroying it")
    void resolvesAndExilesEnchantment() {
        harness.addToBattlefield(player2, new Levitation());
        harness.setHand(player1, List.of(new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Levitation");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Levitation");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Levitation"));
        harness.assertNotInGraveyard(player2, "Levitation");
    }

    @Test
    @DisplayName("Can exile an enchantment controlled by the caster")
    void canExileOwnEnchantment() {
        harness.addToBattlefield(player1, new Levitation());
        harness.setHand(player1, List.of(new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Levitation");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Levitation");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Levitation"));
        harness.assertNotInGraveyard(player1, "Levitation");
    }

    @Test
    @DisplayName("Cannot target an artifact")
    void cannotTargetArtifact() {
        harness.addToBattlefield(player2, new GrimMonolith());
        harness.setHand(player1, List.of(new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grim Monolith");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AngelicCurator());
        harness.setHand(player1, List.of(new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID creatureId = harness.getPermanentId(player2, "Angelic Curator");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
