package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrimalHuntbeast;
import com.github.laxika.magicalvibes.cards.r.RingOfXathrid;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Murder.class, WalkingCorpse.class, Forest.class, PrimalHuntbeast.class, RingOfXathrid.class})
class MurderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys the target creature")
    void resolvingDestroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertInGraveyard(player1, "Murder");
    }

    @Test
    @DisplayName("Can destroy an untapped creature you control")
    void canDestroyOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WalkingCorpse());

        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Murder");
    }

    @Test
    @DisplayName("Cannot target an opponent's hexproof creature")
    void cannotTargetOpponentsHexproofCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PrimalHuntbeast());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Primal Huntbeast");
    }

    @Test
    @DisplayName("Can destroy your own hexproof creature")
    void canDestroyOwnHexproofCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimalHuntbeast());
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primal Huntbeast");
        harness.assertInGraveyard(player1, "Primal Huntbeast");
        harness.assertInGraveyard(player1, "Murder");
    }

    @Test
    @DisplayName("Can destroy a tapped creature")
    void canDestroyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        creature.setTapped(true);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Regeneration prevents Murder's destruction")
    void regenerationPreventsDestruction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new RingOfXathrid());
        ring.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Murder");
    }
}
