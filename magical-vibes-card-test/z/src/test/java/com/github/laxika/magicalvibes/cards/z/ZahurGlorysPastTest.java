package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZahurGlorysPast.class, GrizzlyBears.class, Shock.class, Forest.class})
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

    @Test
    void startsEnginesWhenCastWithoutSpeed() {
        harness.castFromHand(player1, new ZahurGlorysPast(), "{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutAnotherCreatureToSacrifice() {
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Zahur, Glory's Past");
    }

    @Test
    void canKeepTheSurveilledCardOnTop() {
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void anotherCreatureDyingBelowMaxSpeedDoesNotTrigger() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reachingMaxSpeedAfterTheDeathDoesNotCreateAZombie() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void sacrificingAZombieTokenDoesNotReplaceIt() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        harness.addToBattlefield(player1, new GrizzlyBears());
        killWithShock(player1, "Grizzly Bears");
        Permanent zombie = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, zombie.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void opponentsCreatureDyingDoesNotCreateAZombie() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.addToBattlefield(player1, new ZahurGlorysPast());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void zahurDyingBelowMaxSpeedDoesNotTrigger() {
        gd.playerSpeeds.put(player1.getId(), 3);
        Permanent zahur = harness.addToBattlefieldAndReturn(player1, new ZahurGlorysPast());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, zahur.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void killWithShock(Player caster, String targetName) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(caster, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
