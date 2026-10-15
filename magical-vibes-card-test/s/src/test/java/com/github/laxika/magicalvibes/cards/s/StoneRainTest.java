package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Brushland;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneRain.class, Mountain.class, Brushland.class, GrizzlyBears.class})
class StoneRainTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Stone Rain puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Can destroy own land")
    void canDestroyOwnLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player1, "Mountain");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Can destroy a nonbasic land")
    void canDestroyNonbasicLand() {
        harness.addToBattlefield(player2, new Brushland());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Brushland");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Brushland");
        harness.assertInGraveyard(player2, "Brushland");
    }

    @Test
    @DisplayName("Does not destroy an indestructible land")
    void indestructibleLandSurvives() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0, mountain.getId());

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("A regeneration shield prevents land destruction and is consumed")
    void landCanRegenerate() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.setRegenerationShield(1);
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 0, mountain.getId());

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        assertThat(mountain.isTapped()).isTrue();
        assertThat(mountain.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Stone Rain");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Fizzles if target stops being a land before resolution")
    void fizzlesIfTargetStopsBeingLand() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, mountain.getId());

        var targetCard = TestCards.mutableCard(mountain);
        targetCard.setType(CardType.CREATURE);
        targetCard.setPower(2);
        targetCard.setToughness(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Stone Rain");
    }

    @Test
    @DisplayName("Cannot target a creature with Stone Rain")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player with Stone Rain")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys only the targeted land")
    void destroysOnlyTargetedLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Brushland());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Brushland");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotOnBattlefield(player2, "Brushland");
        harness.assertInGraveyard(player2, "Brushland");
    }
}
