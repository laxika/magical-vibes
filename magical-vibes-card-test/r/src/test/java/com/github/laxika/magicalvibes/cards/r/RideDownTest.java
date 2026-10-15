package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.b.BalduvianWarlord;
import com.github.laxika.magicalvibes.cards.f.FlashFoliage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RideDown.class, AlpineGrizzly.class, BalduvianWarlord.class, FlashFoliage.class})
class RideDownTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the blocker and gives trample to the creatures it blocked")
    void destroysBlockerAndGrantsTrample() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        declareBlock(attacker, blocker);

        castRideDown(blocker);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Only creatures blocked by the targeted blocker gain trample")
    void onlyTargetBlockersVictimsGainTrample() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent otherAttacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        Permanent otherBlocker = addCreatureReady(player2, new AlpineGrizzly());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 1)));

        castRideDown(blocker);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(otherBlocker);
    }

    @Test
    @DisplayName("A creature that is not blocking cannot be targeted")
    void nonBlockingCreatureCannotBeTargeted() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        declareBlock(attacker, blocker);
        Permanent bystander = addCreatureReady(player2, new AlpineGrizzly());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new RideDown()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attacker deals full combat damage after its only blocker is destroyed")
    void tramplesOverDestroyedBlocker() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        declareBlock(attacker, blocker);
        harness.setLife(player2, 20);

        castRideDown(blocker);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Trample is granted even when the blocker cannot be destroyed")
    void indestructibleBlockerStillGrantsTrample() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        blocker.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        declareBlock(attacker, blocker);

        castRideDown(blocker);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A regenerated blocker survives but still grants trample to the attacker")
    void regeneratedBlockerStillGrantsTrample() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        blocker.setRegenerationShield(1);
        declareBlock(attacker, blocker);

        castRideDown(blocker);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getRegenerationShield()).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An illegal target on resolution prevents both destruction and trample")
    void targetNoLongerBlockingMakesSpellFizzle() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        declareBlock(attacker, blocker);
        harness.setHand(player1, List.of(new RideDown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, blocker.getId());

        blocker.setBlocking(false);
        blocker.getBlockingTargetIds().clear();
        blocker.getBlockingTargets().clear();
        attacker.setBlockedWithoutBlockers(true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof RideDown);
    }

    @Test
    @DisplayName("A blocker that entered blocking does not grant trample")
    void enteringBlockingDoesNotGrantTrample() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent declaredBlocker = addCreatureReady(player2, new AlpineGrizzly());
        declareBlock(attacker, declaredBlocker);
        harness.setHand(player2, List.of(new FlashFoliage()));
        harness.setLibrary(player2, List.of(new AlpineGrizzly()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        Permanent token = findPermanent(player2, "Saproling");

        castRideDown(token);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures blocked earlier this combat gain trample after the blocker is reassigned")
    void formerBlockedCreatureAlsoGainsTrample() {
        Permanent formerAttacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent newAttacker = addCreatureReady(player1, new AlpineGrizzly());
        Permanent blocker = addCreatureReady(player2, new AlpineGrizzly());
        addCreatureReady(player2, new BalduvianWarlord());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.activateAbility(player2, 1, null, blocker.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, newAttacker.getId());
        assertThat(blocker.getBlockingTargetIds()).containsExactly(newAttacker.getId());

        castRideDown(blocker);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gqs.hasKeyword(gd, formerAttacker, Keyword.TRAMPLE)).isTrue();
    }

    private void castRideDown(Permanent blocker) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new RideDown()));
        harness.castAndResolveInstant(player1, 0, blocker.getId());
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

}
