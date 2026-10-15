package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PistusStrike.class, LeoninSkyhunter.class, PhyrexianDigester.class, VorinclexMonstrousRaider.class})
class PistusStrikeTest extends BaseCardTest {

    @Test
    void castersVorinclexDoublesPoisonPlacedByPistusStrike() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Skyhunter");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    void opponentsVorinclexHalvesPoisonPlacedByPistusStrike() {
        harness.addToBattlefield(player2, new VorinclexMonstrousRaider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Skyhunter");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void givesPoisonEvenWhenTargetRegenerates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Skyhunter");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void givesPoisonEvenWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Skyhunter");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void losingFlyingBeforeResolutionMakesTargetIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        target.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Skyhunter");
        harness.assertInGraveyard(player1, "Pistus Strike");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void canDestroyOwnCreatureAndGiveCasterPoison() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Skyhunter");
        harness.assertInGraveyard(player1, "Leonin Skyhunter");
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Casting Pistus Strike targeting a creature with flying puts it on stack")
    void castingPutsOnStack() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());

        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, angel.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(angel.getId());
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        // Add a creature with flying as valid target so spell is playable
        harness.addToBattlefield(player1, new LeoninSkyhunter());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new PhyrexianDigester());

        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Resolving Pistus Strike destroys target creature and gives controller a poison counter")
    void resolvingDestroysAndGivesPoison() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());

        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature is destroyed
        harness.assertNotOnBattlefield(player2, "Leonin Skyhunter");
        harness.assertInGraveyard(player2, "Leonin Skyhunter");
        // Controller gets a poison counter
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        // Caster does NOT get a poison counter
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        // Pistus Strike goes to graveyard
        harness.assertInGraveyard(player1, "Pistus Strike");
    }

    @Test
    @DisplayName("Pistus Strike fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());

        harness.setHand(player1, List.of(new PistusStrike()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, angel.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // No poison counter since spell fizzled
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        // Pistus Strike goes to graveyard
        harness.assertInGraveyard(player1, "Pistus Strike");
    }
}
