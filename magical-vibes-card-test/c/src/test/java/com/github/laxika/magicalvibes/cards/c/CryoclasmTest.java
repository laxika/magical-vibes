package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cryoclasm.class, Island.class, Mountain.class, Plains.class, TrueBeliever.class})
class CryoclasmTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cryoclasm puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target Plains and deals 3 damage to its controller")
    void resolvingDestroysTargetPlainsAndDealsDamage() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertInGraveyard(player2, "Plains");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Resolving destroys target Island and deals 3 damage to its controller")
    void resolvingDestroysTargetIslandAndDealsDamage() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player2, "Island");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Can target own Plains")
    void canTargetOwnPlains() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player1, "Plains");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Plains");
        // Damage dealt to self (player1 is the land's controller)
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Fizzles if target land is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // No damage dealt because spell fizzled
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a Mountain")
    void cannotTargetMountain() {
        harness.addToBattlefield(player1, new Plains()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID mountainId = harness.getPermanentId(player2, "Mountain");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, mountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Plains or Island");
    }

    @Test
    @DisplayName("Cryoclasm goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cryoclasm");
    }
    @Test
    @DisplayName("Damage still happens when the target land regenerates")
    void dealsDamageWhenLandRegenerates() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        land.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        harness.assertOnBattlefield(player2, "Plains");
        assertThat(land.isTapped()).isTrue();
        assertThat(land.getRegenerationShield()).isZero();
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A land that stops being a Plains or Island is an illegal target at resolution")
    void noDamageWhenLandLosesEligibleSubtype() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, land.getId());

        land.setPersistentLandTypeOverride(CardSubtype.MOUNTAIN);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Plains");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Cryoclasm");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Damage does not target the land's controller and ignores their shroud")
    void dealsDamageToControllerWithShroud() {
        harness.addToBattlefield(player2, new TrueBeliever());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Cryoclasm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player2, "Island");
        harness.assertOnBattlefield(player2, "True Believer");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }
}

