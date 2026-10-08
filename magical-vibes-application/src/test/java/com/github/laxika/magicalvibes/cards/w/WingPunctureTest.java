package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingPuncture.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class, GiantGrowth.class})
class WingPunctureTest extends BaseCardTest {

    @Test
    @DisplayName("Creature deals nonlethal power damage to a flying creature")
    void dealsNonlethalDamageToFlyingCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveInstant(player1, 0, List.of(bearId, elementalId));

        // Air Elemental should survive (2 damage < 4 toughness)
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Large creature kills flying creature with power damage")
    void largeCreatureKillsFlyingCreature() {
        // Air Elemental (4/4 flying) as biter deals 4 damage to opponent's Air Elemental (4/4 flying)
        harness.addToBattlefield(player1, new AirElemental());
        AirElemental opponentFlyer = new AirElemental();
        harness.addToBattlefield(player2, opponentFlyer);
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID biterId = harness.getPermanentId(player1, "Air Elemental");
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castAndResolveInstant(player1, 0, List.of(biterId, targetId));

        // Opponent's Air Elemental should be destroyed (4 damage = 4 toughness)
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying as second target")
    void cannotTargetNonFlyingCreatureAsSecondTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Cannot target opponent's creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // needed so the spell is castable
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elementalId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Spell fizzles when all targets removed before resolution")
    void fizzlesWhenAllTargetsRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(bearId, elementalId));

        // Remove both targets before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Bite does nothing when biter removed before resolution")
    void biteDoesNothingWhenBiterRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elementalId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(bearId, elementalId));

        // Remove the biter before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Air Elemental should survive — no biter to deal damage
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void flyingCreatureCanDealDamageToItself() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, List.of(flyer.getId(), flyer.getId()));

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
    }

    @Test
    void canDamageAnotherCreatureYouControlWithoutReturnDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), flyer.getId()));

        assertThat(flyer.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void usesPowerAtResolutionAfterGiantGrowth() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, List.of(source.getId(), flyer.getId()));
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void dealsNoDamageWhenVictimLosesFlying() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(source.getId(), flyer.getId()));
        flyer.getRemovedKeywords().add(Keyword.FLYING);

        harness.passBothPriorities();

        assertThat(flyer.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Wing Puncture");
    }

    @Test
    void dealsNoDamageWhenSourceChangesController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new WingPuncture()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(source.getId(), flyer.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);

        harness.passBothPriorities();

        assertThat(flyer.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Wing Puncture");
    }
}
