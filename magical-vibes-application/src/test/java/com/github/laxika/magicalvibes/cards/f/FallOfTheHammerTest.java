package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallOfTheHammer.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class})
class FallOfTheHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Creature you control deals damage equal to its power to another creature")
    void dealsPowerDamageToAnotherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(sourceId, targetId));

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Another creature may be controlled by the caster")
    void mayTargetOwnOtherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(sourceId, targetId));

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("First target must be a creature you control")
    void cannotTargetOpponentCreatureAsSource() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(sourceId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("The two targets must be different creatures")
    void cannotTargetSameCreatureTwice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creatureId, creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No damage is dealt if the source creature leaves before resolution")
    void dealsNoDamageWhenSourceLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID sourceId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.castInstant(player1, 0, List.of(sourceId, targetId));
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Damage uses the source creature's power at resolution")
    void usesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        source.setPowerModifier(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The recipient does not deal damage back to the source")
    void doesNotFight() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("No damage is dealt if another player controls the source at resolution")
    void dealsNoDamageWhenSourceChangesController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Fall of the Hammer");
    }

    @Test
    @DisplayName("No damage is dealt if the recipient leaves before resolution")
    void dealsNoDamageWhenRecipientLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new FallOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fall of the Hammer");
    }
}
