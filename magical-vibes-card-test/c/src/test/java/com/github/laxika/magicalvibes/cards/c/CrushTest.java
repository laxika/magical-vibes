package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.i.InkmothNexus;
import com.github.laxika.magicalvibes.cards.s.SilverskinArmor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Crush.class, FountainOfYouth.class, CoreProwler.class, InkmothNexus.class, SilverskinArmor.class})
class CrushTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Crush puts it on the stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Crush destroys target noncreature artifact")
    void destroysNoncreatureArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Cannot target an artifact creature with Crush")
    void cannotTargetArtifactCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new CoreProwler()).getId();
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crush fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Crush");
    }

    @Test
    @DisplayName("Crush goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth()).getId();
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Crush");
    }

    @Test
    @DisplayName("Crush can destroy its controller's noncreature artifact")
    void destroysOwnNoncreatureArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new SilverskinArmor()).getId();
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Silverskin Armor");
        harness.assertInGraveyard(player1, "Silverskin Armor");
        harness.assertInGraveyard(player1, "Crush");
    }

    @Test
    @DisplayName("Crush cannot target a nonartifact land")
    void cannotTargetNonartifactLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new InkmothNexus()).getId();
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Inkmoth Nexus");
        harness.assertInHand(player1, "Crush");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Crush cannot target a land that becomes an artifact creature")
    void cannotTargetAnimatedArtifactLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new InkmothNexus()).getId();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Crush()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Inkmoth Nexus");
        harness.assertInHand(player1, "Crush");
        assertThat(gd.stack).isEmpty();
    }
}
