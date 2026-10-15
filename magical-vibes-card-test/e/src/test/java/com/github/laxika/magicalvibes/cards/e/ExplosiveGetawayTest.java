package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Explosive Getaway")
@CardUsed({ExplosiveGetaway.class, GrizzlyBears.class, HillGiant.class, Forest.class,
        AvatarOfMight.class, CrawWurm.class, Spellbook.class})
class ExplosiveGetawayTest extends BaseCardTest {

    @Test
    @DisplayName("exiles a targeted creature, damages the other creatures, and returns the target at the next end step")
    void exilesTargetAndReturnsAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        castGetaway(target.getId());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player1, "Hill Giant")).isZero();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("can resolve without choosing the optional target")
    void canResolveWithoutTarget() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.castFromHand(player1, new ExplosiveGetaway(), "{3}{R}{W}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hill Giant")).isZero();
    }

    @Test
    @DisplayName("rejects a land as the optional target")
    void rejectsLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ExplosiveGetaway()));
        addGetawayMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesNoncreatureArtifactAndReturnsItUntapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        target.tap();
        harness.addToBattlefield(player1, new HillGiant());
        castGetaway(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    void dealsExactlyFourDamageToBothPlayersCreaturesButNotPlayers() {
        harness.addToBattlefield(player1, new CrawWurm());
        harness.addToBattlefield(player2, new CrawWurm());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setLife(player1, 17);
        harness.setLife(player2, 19);

        harness.castFromHand(player1, new ExplosiveGetaway(), "{3}{R}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Craw Wurm");
        harness.assertInGraveyard(player2, "Craw Wurm");
        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(survivor.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 19);
    }

    @Test
    void returnsStolenCreatureToOwnerRatherThanController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        castGetaway(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isZero();
    }

    @Test
    void doesNotDealDamageWhenChosenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castGetaway(target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(survivor.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Explosive Getaway");
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void castGetaway(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ExplosiveGetaway()));
        addGetawayMana();
        harness.castSorcery(player1, 0, 0, targetId);
    }

    private void addGetawayMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
