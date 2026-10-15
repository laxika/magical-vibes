package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HashepOasis;
import com.github.laxika.magicalvibes.cards.h.HopeTender;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfForgottenPharaohs.class, HashepOasis.class, HopeTender.class, NicolBolasGodPharaoh.class})
class WallOfForgottenPharaohsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage when you control a Desert")
    void dealsDamageWithDesertOnBattlefield() {
        harness.setLife(player2, 20);
        Permanent wall = addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.addToBattlefield(player1, new HashepOasis());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability deals 1 damage when a Desert is in your graveyard")
    void dealsDamageWithDesertInGraveyard() {
        harness.setLife(player2, 20);
        Permanent wall = addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.setGraveyard(player1, List.of(new HashepOasis()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot activate without a Desert on battlefield or in graveyard")
    void cannotActivateWithoutDesert() {
        addCreatureReady(player1, new WallOfForgottenPharaohs());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Desert");
    }

    @Test
    @DisplayName("Ability cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.addToBattlefield(player1, new HashepOasis());
        harness.addToBattlefield(player2, new HopeTender());
        UUID creatureId = harness.getPermanentId(player2, "Hope Tender");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnPlayer() {
        addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.addToBattlefield(player1, new HashepOasis());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void dealsDamageToPlaneswalker() {
        addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.addToBattlefield(player1, new HashepOasis());
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 7);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, bolas.getId());
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentDesertsDoNotAllowActivation() {
        Permanent wall = addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.addToBattlefield(player2, new HashepOasis());
        harness.setGraveyard(player2, List.of(new HashepOasis()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Desert");

        assertThat(wall.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void desertInHandOrExileDoesNotAllowActivation() {
        addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.setHand(player1, List.of(new HashepOasis()));
        harness.setExile(player1, List.of(new HashepOasis()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Desert");
    }

    @Test
    void removingDesertAfterActivationDoesNotStopDamage() {
        addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.setGraveyard(player1, List.of(new HashepOasis()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void removingWallAfterActivationDoesNotStopDamage() {
        Permanent wall = addCreatureReady(player1, new WallOfForgottenPharaohs());
        harness.setGraveyard(player1, List.of(new HashepOasis()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wall);
        harness.setGraveyard(player1, List.of(new HashepOasis(), wall.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void summoningSicknessPreventsActivation() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfForgottenPharaohs());
        wall.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new HashepOasis()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void tappedWallCannotActivate() {
        Permanent wall = addCreatureReady(player1, new WallOfForgottenPharaohs());
        wall.tap();
        harness.setGraveyard(player1, List.of(new HashepOasis()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
