package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

@CardUsed({IntoTheMawOfHell.class, Mountain.class, WalkingCorpse.class, ManorGargoyle.class})
class IntoTheMawOfHellTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land and deals 13 damage to target creature")
    void destroysLandAndDealsDamageToCreature() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveSorcery(player1, 0, List.of(landId, creatureId));

        // Land should be destroyed
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        // Creature should be destroyed (13 damage >= 2 toughness)
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Can target own land and opponent's creature")
    void canTargetOwnLandAndOpponentCreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player1, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveSorcery(player1, 0, List.of(landId, creatureId));

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Cannot target a creature as first target")
    void cannotTargetCreatureAsFirstTarget() {
        harness.addToBattlefield(player2, new Mountain()); // needed so the spell is castable
        UUID creature1Id = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse()).getId();
        UUID creature2Id = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse()).getId();
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature1Id, creature2Id)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Cannot target a land as second target")
    void cannotTargetLandAsSecondTarget() {
        UUID land1Id = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        UUID land2Id = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.addToBattlefield(player2, new WalkingCorpse()); // needed so the spell is castable
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land1Id, land2Id)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));

        // Remove both targets before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Destroy still happens when creature target removed before resolution")
    void destroyStillHappensWhenCreatureTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));

        // Remove only the creature target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Walking Corpse"));

        harness.passBothPriorities();

        // Land should still be destroyed
        harness.assertNotOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Damage still happens when land target removed before resolution")
    void damageStillHappensWhenLandTargetRemoved() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castSorcery(player1, 0, List.of(landId, creatureId));

        // Remove only the land target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Mountain"));

        harness.passBothPriorities();

        // Creature should still take 13 damage and die
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID landId = harness.getPermanentId(player2, "Mountain");
        UUID creatureId = harness.getPermanentId(player2, "Walking Corpse");
        harness.castAndResolveSorcery(player1, 0, List.of(landId, creatureId));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Into the Maw of Hell");
    }

    @Test
    @DisplayName("Deals exactly 13 damage to an indestructible creature")
    void dealsExactlyThirteenDamage() {
        UUID landId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ManorGargoyle());
        harness.setHand(player1, List.of(new IntoTheMawOfHell()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, List.of(landId, creature.getId()));

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Manor Gargoyle");
        assertThat(creature.getMarkedDamage()).isEqualTo(13);
    }
}