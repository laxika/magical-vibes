package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZahurGlorysPastTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndSurveilsOncePerTurn() {
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstCreature);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(secondCreature).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void maxSpeedCreatesTappedZombieWhenAnotherNontokenCreatureDies() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player1, "Grizzly Bears");

        Permanent zombie = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(zombie.isTapped()).isTrue();
    }

    @Test
    void maxSpeedDoesNotCreateZombieBelowMaxSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player1, "Grizzly Bears");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void maxSpeedCreatesZombieWhenZahurDies() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addToBattlefield(player1, new ZahurGlorysPast());

        killWithShock(player1, "Zahur, Glory's Past");

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    private void killWithShock(Player caster, String targetName) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(caster, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
