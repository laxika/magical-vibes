package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.Anoint;
import com.github.laxika.magicalvibes.cards.f.FieryEmancipation;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WallOfDiffusion;
import com.github.laxika.magicalvibes.cards.w.WallOfSwords;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({Repentance.class, GrizzlyBears.class, Plains.class, WallOfSwords.class,
        Rootwalla.class, WallOfDiffusion.class, Anoint.class, FieryEmancipation.class})
class RepentanceTest extends BaseCardTest {

    @Test
    @DisplayName("Repentance kills a 2/2 when it deals 2 damage to itself")
    void killsCreatureWhenPowerIsLethal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Repentance leaves a 3/5 alive with 3 marked damage")
    void survivesWhenPowerIsBelowToughness() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, wall.getId());
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Repentance can target a creature its caster controls")
    void canTargetOwnCreature() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfSwords());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, wall.getId());
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID plainsId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plainsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Repentance fizzles when target is gone before resolution")
    void fizzlesWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Repentance");
    }

    @Test
    void zeroPowerCreatureDealsNoDamage() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfDiffusion());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, wall.getId());

        harness.assertOnBattlefield(player2, "Wall of Diffusion");
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    void usesPowerAtResolutionAfterResponse() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player2, new Rootwalla());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, rootwalla.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rootwalla");
    }

    @Test
    void damageCanBePrevented() {
        Permanent rootwalla = harness.addToBattlefieldAndReturn(player2, new Rootwalla());
        harness.setHand(player1, List.of(new Repentance()));
        harness.setHand(player2, List.of(new Anoint()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, rootwalla.getId());
        harness.castAndResolveInstant(player2, 0, rootwalla.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rootwalla");
        assertThat(rootwalla.getMarkedDamage()).isZero();
    }

    @Test
    void castersDamageMultiplierDoesNotApplyToOpponentsCreature() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, wall.getId());

        harness.assertOnBattlefield(player2, "Wall of Swords");
        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void creaturesControllersDamageMultiplierAppliesToSelfDamage() {
        harness.addToBattlefield(player2, new FieryEmancipation());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfSwords());
        harness.setHand(player1, List.of(new Repentance()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, wall.getId());

        harness.assertInGraveyard(player2, "Wall of Swords");
    }
}
