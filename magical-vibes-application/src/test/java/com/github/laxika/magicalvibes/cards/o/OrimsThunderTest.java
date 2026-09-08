package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrimsThunder.class, GrizzlyBears.class, RodOfRuin.class})
class OrimsThunderTest extends BaseCardTest {

    private void addOrimsThunderMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Without kicker, destroys the artifact without needing a creature target")
    void destroysArtifactWithoutKicker() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Rod of Ruin");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, artifactId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .anyMatch(permanent -> permanent.getId().equals(creatureId))).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(creatureId))
                .findFirst().orElseThrow().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("When kicked, deals damage equal to the destroyed artifact's mana value")
    void kickedDealsArtifactManaValueDamageToCreature() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Rod of Ruin");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castKickedInstantWithSacrifices(player1, 0, artifactId, List.of(creatureId), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("If the artifact target is illegal, the kicked damage is not dealt")
    void kickedDoesNotDamageWhenArtifactTargetIsRemoved() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Rod of Ruin");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castKickedInstantWithSacrifices(player1, 0, artifactId, List.of(creatureId), List.of());
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(artifactId));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(creatureId))
                .findFirst().orElseThrow().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("If the creature target is illegal, still destroys the artifact")
    void kickedStillDestroysWhenCreatureTargetIsRemoved() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID artifactId = harness.getPermanentId(player2, "Rod of Ruin");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castKickedInstantWithSacrifices(player1, 0, artifactId, List.of(creatureId), List.of());
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(creatureId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Cannot target a creature for the artifact or enchantment target")
    void cannotTargetCreatureAsPermanentTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrimsThunder()));
        addOrimsThunderMana();

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
