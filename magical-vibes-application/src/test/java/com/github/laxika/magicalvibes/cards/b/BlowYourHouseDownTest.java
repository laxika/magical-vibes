package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.r.RagingRedcap;
import com.github.laxika.magicalvibes.cards.r.RovingKeep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlowYourHouseDown.class, BakeIntoAPie.class, GoldenEgg.class, RagingRedcap.class, RovingKeep.class})
class BlowYourHouseDownTest extends BaseCardTest {

    @Test
    @DisplayName("Makes up to three target creatures unable to block and destroys targeted Walls")
    void makesCreaturesUnableToBlockAndDestroysWalls() {
        Permanent wall1 = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        Permanent wall2 = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        Permanent untargetedWall = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.of(wall1.getId(), bear.getId(), wall2.getId()));

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).extracting(Permanent::getId)
                .containsExactlyInAnyOrder(bear.getId(), untargetedWall.getId());
        assertThat(bear.isCantBlockThisTurn()).isTrue();
        assertThat(untargetedWall.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId())
                .contains(wall1.getCard().getId(), wall2.getCard().getId());
    }

    @Test
    @DisplayName("Allows fewer than three targets")
    void allowsFewerThanThreeTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId()));

        assertThat(bear.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());
        prepareCast();

        UUID artifactId = artifact.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void allowsZeroTargetsWithoutAffectingCreatures() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.<UUID>of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blow Your House Down");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall, creature);
        assertThat(wall.isCantBlockThisTurn()).isFalse();
        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void rejectsMoreThanThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsTheSameCreatureChosenTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetCreaturesControlledByBothPlayers() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new RovingKeep());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RagingRedcap());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        prepareCast();

        harness.castAndResolveSorcery(player1, 0,
                List.of(wall.getId(), ownCreature.getId(), opposingCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wall);
        harness.assertInGraveyard(player1, "Roving Keep");
        assertThat(ownCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opposingCreature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void blockingRestrictionExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));
        assertThat(creature.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void stillAffectsLegalTargetsWhenOneTargetIsDestroyedInResponse() {
        Permanent removedCreature = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        Permanent survivingCreature = harness.addToBattlefieldAndReturn(player2, new RagingRedcap());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        prepareCast();
        harness.setHand(player2, List.of(new BakeIntoAPie()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0,
                List.of(removedCreature.getId(), survivingCreature.getId(), wall.getId()));
        harness.castAndResolveInstant(player2, 0, removedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(survivingCreature).doesNotContain(removedCreature, wall);
        assertThat(survivingCreature.isCantBlockThisTurn()).isTrue();
        assertThat(removedCreature.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId())
                .contains(removedCreature.getCard().getId(), wall.getCard().getId());
    }

    @Test
    void indestructibleWallSurvivesButCannotBlock() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new RovingKeep());
        wall.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        prepareCast();

        harness.castAndResolveSorcery(player1, 0, List.of(wall.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        harness.assertNotInGraveyard(player2, "Roving Keep");
        assertThat(wall.isCantBlockThisTurn()).isTrue();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new BlowYourHouseDown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
