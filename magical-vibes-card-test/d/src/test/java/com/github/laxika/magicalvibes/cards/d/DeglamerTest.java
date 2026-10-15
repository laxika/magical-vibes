package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.c.ChameleonColossus;
import com.github.laxika.magicalvibes.cards.c.CloakAndDagger;
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

@CardUsed({Deglamer.class, CloakAndDagger.class, Bitterblossom.class, ChameleonColossus.class})
class DeglamerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving shuffles target artifact into its owner's library")
    void shufflesArtifactIntoLibrary() {
        harness.addToBattlefield(player2, new CloakAndDagger());
        harness.setHand(player1, List.of(new Deglamer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Cloak and Dagger");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Cloak and Dagger");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cloak and Dagger"));
        harness.assertNotInGraveyard(player2, "Cloak and Dagger");
    }

    @Test
    @DisplayName("Resolving shuffles target enchantment into its owner's library")
    void shufflesEnchantmentIntoLibrary() {
        harness.addToBattlefield(player2, new Bitterblossom());
        harness.setHand(player1, List.of(new Deglamer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Bitterblossom");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Bitterblossom");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Bitterblossom"));
    }

    @Test
    @DisplayName("Resolving shuffles the target into its owner's library rather than its controller's")
    void shufflesIntoOwnersLibrary() {
        CloakAndDagger target = new CloakAndDagger();
        target.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, target);
        harness.setHand(player1, List.of(new Deglamer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Cloak and Dagger");
        gd.stolenCreatures.put(targetId, player1.getId());
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Cloak and Dagger");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cloak and Dagger"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Cloak and Dagger"));
    }

    @Test
    @DisplayName("Cannot target a creature with Deglamer")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new ChameleonColossus());
        harness.setHand(player1, List.of(new Deglamer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Chameleon Colossus");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can shuffle your own enchantment into an empty library")
    void shufflesOwnEnchantmentIntoEmptyLibrary() {
        Bitterblossom target = new Bitterblossom();
        harness.addToBattlefield(player1, target);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Deglamer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Bitterblossom"));

        harness.assertNotOnBattlefield(player1, "Bitterblossom");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
        harness.assertNotInGraveyard(player1, "Bitterblossom");
        harness.assertInGraveyard(player1, "Deglamer");
    }

    @Test
    @DisplayName("Does not move a target already shuffled away in response")
    void targetLeavesBeforeResolution() {
        CloakAndDagger target = new CloakAndDagger();
        harness.addToBattlefield(player2, target);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Deglamer(), new Deglamer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Cloak and Dagger");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloak and Dagger");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Deglamer"))
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
