package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntoTheMawOfHellTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and deals 13 damage to target creature")
    void destroysLandAndDealsDamageToCreature() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Land should be destroyed
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        // Creature should be destroyed (13 damage >= 2 toughness)
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target own land and opponent's creature")
    void canTargetOwnLandAndOpponentCreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player1, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature as first target")
    void cannotTargetCreatureAsFirstTarget() {
        harness.addToBattlefield(player2, new Mountain()); // needed so the spell is castable
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears secondBear = new GrizzlyBears();
        harness.addToBattlefield(player2, secondBear);
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID creature1Id = bf.stream().filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().get().getId();
        UUID creature2Id = bf.stream().filter(p -> p.getCard().getName().equals("Grizzly Bears") && !p.getId().equals(creature1Id)).findFirst().get().getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature1Id, creature2Id)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Cannot target a land as second target")
    void cannotTargetLandAsSecondTarget() {
        Mountain mountain1 = new Mountain();
        Mountain mountain2 = new Mountain();
        harness.addToBattlefield(player2, mountain1);
        harness.addToBattlefield(player2, mountain2);
        harness.addToBattlefield(player2, new GrizzlyBears()); // needed so the spell is castable
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID land1Id = harness.getPermanentId(player2, mountain1.getName());
        List<Permanent> bf = harness.getGameData().playerBattlefields.get(player2.getId());
        UUID land2Id = bf.stream()
                .filter(p -> p.getCard().getName().equals("Mountain") && !p.getId().equals(land1Id))
                .findFirst().get().getId();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land1Id, land2Id)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));

        // Remove both targets before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Destroy still happens when creature target removed before resolution")
    void destroyStillHappensWhenCreatureTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));

        // Remove only the creature target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Land should still be destroyed
        harness.assertNotOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Damage still happens when land target removed before resolution")
    void damageStillHappensWhenLandTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));

        // Remove only the land target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Mountain"));

        harness.passBothPriorities();

        // Creature should still take 13 damage and die
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Into the Maw of Hell"));
    }
}
