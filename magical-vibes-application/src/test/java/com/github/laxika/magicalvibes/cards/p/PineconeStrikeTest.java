package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.OrdinaryBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PineconeStrike.class, OrdinaryBear.class})
class PineconeStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 3 damage and exiles a creature that dies")
    void damageModeExilesCreatureThatDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, createCreature("Target Creature", 2, 2));

        cast(new int[]{0}, List.of(creature.getId()));

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Target Creature");
        harness.assertNotInGraveyard(player2, "Target Creature");
        assertThat(gameData.exiledCards).anyMatch(entry -> entry.card().getName().equals("Target Creature"));
    }

    @Test
    @DisplayName("Destroy mode destroys an artifact token")
    void destroyModeDestroysArtifactToken() {
        Permanent token = harness.addToBattlefieldAndReturn(
                player2, createArtifactToken("Treasure Token", false));

        cast(new int[]{1}, List.of(token.getId()));

        harness.assertNotOnBattlefield(player2, "Treasure Token");
    }

    @Test
    @DisplayName("Both modes may target the same artifact creature token")
    void bothModesMayTargetSameArtifactCreatureToken() {
        Permanent token = harness.addToBattlefieldAndReturn(
                player2, createArtifactToken("Construct Token", true));

        cast(new int[]{0, 1}, List.of(token.getId(), token.getId()));

        harness.assertNotOnBattlefield(player2, "Construct Token");
    }

    @Test
    @DisplayName("Destroy mode cannot target a nontoken artifact")
    void destroyModeRejectsNontokenArtifact() {
        Card artifactCard = createArtifactToken("Artifact", false);
        artifactCard.setToken(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(
                player2, artifactCard);

        harness.setHand(player1, List.of(new PineconeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A surviving creature takes exactly 3 damage and is exiled if it dies later this turn")
    void survivingCreatureIsExiledByLaterLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OrdinaryBear());

        cast(new int[]{0}, List.of(creature.getId()));

        harness.assertOnBattlefield(player2, "Ordinary Bear");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);

        cast(new int[]{0}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Ordinary Bear");
        harness.assertNotInGraveyard(player2, "Ordinary Bear");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Ordinary Bear"));
    }

    @Test
    @DisplayName("Both modes resolve against different targets")
    void bothModesResolveAgainstDifferentTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OrdinaryBear());
        Permanent token = harness.addToBattlefieldAndReturn(
                player2, createArtifactToken("Treasure Token", false));

        cast(new int[]{0, 1}, List.of(creature.getId(), token.getId()));

        harness.assertOnBattlefield(player2, "Ordinary Bear");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Treasure Token");
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new PineconeStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Card createArtifactToken(String name, boolean creature) {
        Card card = new Card();
        card.setName(name);
        card.setType(creature ? CardType.CREATURE : CardType.ARTIFACT);
        card.setAdditionalTypes(creature ? Set.of(CardType.ARTIFACT) : Set.of());
        card.setManaCost("");
        card.setColor(null);
        card.setToken(true);
        if (creature) {
            card.setPower(4);
            card.setToughness(4);
        }
        return card;
    }
}
