package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SteadfastGuard;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RainOfTears.class, Mountain.class, RishadanPort.class, SteadfastGuard.class})
class RainOfTearsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rain of Tears puts it on the stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Can destroy own land")
    void canDestroyOwnLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Rain of Tears goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("Can destroy a nonbasic land")
    void canDestroyNonbasicLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new RishadanPort()).getId();
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Rishadan Port");
        harness.assertInGraveyard(player2, "Rishadan Port");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("Fizzles if target stops being a land before resolution")
    void fizzlesIfTargetStopsBeingLand() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, mountain.getId());

        var targetCard = TestCards.mutableCard(mountain);
        targetCard.setType(CardType.CREATURE);
        targetCard.setPower(1);
        targetCard.setToughness(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("Cannot destroy a creature with Rain of Tears")
    void cannotDestroyCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new SteadfastGuard()).getId();
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An indestructible land survives Rain of Tears")
    void indestructibleLandSurvives() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Rain of Tears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A regeneration shield saves the targeted land")
    void landCanRegenerate() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        mountain.setRegenerationShield(1);
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        assertThat(mountain.isTapped()).isTrue();
        assertThat(mountain.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Rain of Tears");
    }

    @Test
    @DisplayName("A land that becomes a creature remains a legal target")
    void landBecomingCreatureIsStillDestroyed() {
        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new RainOfTears()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, mountain.getId());

        var targetCard = TestCards.mutableCard(mountain);
        targetCard.setAdditionalTypes(Set.of(CardType.CREATURE));
        targetCard.setPower(3);
        targetCard.setToughness(3);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player1, "Rain of Tears");
    }
}
