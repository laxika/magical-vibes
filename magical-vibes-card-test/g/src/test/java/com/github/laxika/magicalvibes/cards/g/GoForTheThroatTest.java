package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.MyrSire;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.r.RotWolf;
import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.p.Phyresis;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoForTheThroat.class, MyrSire.class, RotWolf.class, PhyrexianRager.class,
        Phyresis.class, DarksteelPlate.class})
class GoForTheThroatTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Go for the Throat targeting a nonartifact creature puts it on stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RotWolf());

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        // Add a nonartifact creature as valid target so spell is playable
        harness.addToBattlefield(player1, new RotWolf());

        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new MyrSire());

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact creature");
    }

    @Test
    @DisplayName("Can target a black creature unlike Terror")
    void canTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new PhyrexianRager());

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, blackCreature.getId());

        harness.assertNotOnBattlefield(player2, "Phyrexian Rager");
        harness.assertInGraveyard(player2, "Phyrexian Rager");
    }

    @Test
    @DisplayName("Resolving Go for the Throat destroys target creature and moves it to graveyard")
    void resolvingDestroysTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RotWolf());

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Rot Wolf");
        harness.assertInGraveyard(player2, "Rot Wolf");
        harness.assertInGraveyard(player1, "Go for the Throat");
    }

    @Test
    @DisplayName("Go for the Throat allows regeneration")
    void allowsRegeneration() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RotWolf());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Rot Wolf");
        harness.assertNotInGraveyard(player2, "Rot Wolf");
    }

    @Test
    @DisplayName("Go for the Throat fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RotWolf());

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Go for the Throat");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RotWolf());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Phyresis());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Phyresis");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("A target that becomes an artifact is illegal on resolution")
    void fizzlesIfTargetBecomesArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RotWolf());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());

        creature.getPersistentGrantedCardTypes().add(CardType.ARTIFACT);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rot Wolf");
        harness.assertNotInGraveyard(player2, "Rot Wolf");
        harness.assertInGraveyard(player1, "Go for the Throat");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("An indestructible nonartifact creature survives")
    void indestructibleCreatureSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RotWolf());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new DarksteelPlate());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Rot Wolf");
        harness.assertNotInGraveyard(player2, "Rot Wolf");
        harness.assertInGraveyard(player1, "Go for the Throat");
    }

    @Test
    @DisplayName("Can destroy its controller's creature")
    void canDestroyOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Rot Wolf");
        harness.assertInGraveyard(player1, "Rot Wolf");
        harness.assertInGraveyard(player1, "Go for the Throat");
    }
}
