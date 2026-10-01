package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.cards.r.RimeboundDead;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Deathmark.class, BorealDruid.class, KjeldoranOutrider.class, RimeboundDead.class,
        SnowCoveredPlains.class})
class DeathmarkTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a green creature")
    void canTargetGreenCreature() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, druid.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(druid.getId());
    }

    @Test
    @DisplayName("Can target a green creature controlled by the caster")
    void canTargetOwnGreenCreature() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, druid.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(druid.getId());
    }

    @Test
    @DisplayName("Can target a white creature")
    void canTargetWhiteCreature() {
        Permanent outrider = harness.addToBattlefieldAndReturn(player2, new KjeldoranOutrider());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, outrider.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(outrider.getId());
    }

    @Test
    @DisplayName("Cannot target a non-green non-white creature")
    void cannotTargetBlackCreature() {
        // Add a green creature as valid target so spell is playable
        harness.addToBattlefield(player1, new BorealDruid());

        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new RimeboundDead());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, skeletons.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green or white");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        // Add a green creature as valid target so spell is playable
        harness.addToBattlefield(player1, new BorealDruid());

        Permanent plains = harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green or white creature");
    }

    @Test
    @DisplayName("Resolving destroys target green creature")
    void resolvingDestroysGreenCreature() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, druid.getId());

        harness.assertNotOnBattlefield(player2, "Boreal Druid");
        harness.assertInGraveyard(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Deathmark goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, druid.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deathmark");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, druid.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deathmark");
    }
}

