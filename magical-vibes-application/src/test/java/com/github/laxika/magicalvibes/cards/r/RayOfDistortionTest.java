package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RayOfDistortion.class, FountainOfYouth.class, GloriousAnthem.class, GrizzlyBears.class, Ornithopter.class})
class RayOfDistortionTest extends BaseCardTest {

    @Test
    @DisplayName("Ray of Distortion destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Ray of Distortion destroys a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Ray of Distortion cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ray of Distortion can destroy an enchantment with flashback and is exiled")
    void flashbackDestroysEnchantmentAndExilesSpell() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setGraveyard(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castAndResolveFlashback(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertNotInGraveyard(player1, "Ray of Distortion");

        GameData gd = harness.getGameData();
        org.assertj.core.api.Assertions.assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ray of Distortion"));
    }

    @Test
    @DisplayName("Ray of Distortion destroys an artifact creature")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Ornithopter"));

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Ray of Distortion");
    }

    @Test
    @DisplayName("Ray of Distortion can destroy an artifact you control")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Fountain of Youth"));

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Ray of Distortion can be cast normally and then flashed back")
    void normalCastThenFlashback() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Glorious Anthem"));
        harness.assertInGraveyard(player1, "Ray of Distortion");

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveFlashback(player1, 0, harness.getPermanentId(player2, "Fountain of Youth"));

        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Ray of Distortion");
        org.assertj.core.api.Assertions.assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ray of Distortion"));
    }

    @Test
    @DisplayName("Flashback requires two white mana")
    void flashbackRequiresTwoWhiteMana() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Ray of Distortion");
    }

    @Test
    @DisplayName("Flashback requires six total mana")
    void flashbackRequiresSixMana() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Ray of Distortion");
    }

    @Test
    @DisplayName("A flashback spell is exiled when its target disappears before resolution")
    void flashbackExiledWhenTargetDisappears() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new RayOfDistortion()));
        harness.setHand(player2, List.of(new RayOfDistortion()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player2, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castFlashback(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Ray of Distortion");
        harness.assertNotInGraveyard(player1, "Ray of Distortion");
        org.assertj.core.api.Assertions.assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Ray of Distortion"));
    }
}
