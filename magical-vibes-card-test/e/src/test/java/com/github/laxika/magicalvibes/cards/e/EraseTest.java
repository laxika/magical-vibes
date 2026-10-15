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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Levitation()).getId();
        harness.setHand(player1, List.of(new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 1);

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
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Levitation()).getId();
        harness.setHand(player1, List.of(new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 1);

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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrimMonolith()).getId();
        harness.setHand(player1, List.of(new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new AngelicCurator()).getId();
        harness.setHand(player1, List.of(new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not exile another enchantment when its target leaves before resolution")
    void targetLeavingBeforeResolutionDoesNotAffectOtherEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Levitation()).getId();
        UUID otherId = harness.addToBattlefieldAndReturn(player1, new Levitation()).getId();
        harness.setHand(player1, List.of(new Erase(), new Erase()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertNotOnBattlefield(player2, "Levitation");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(otherId));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Levitation"))
                .hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Erase"))
                .hasSize(2);
    }
}
