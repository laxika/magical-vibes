package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GutTrueSoulZealot.class, GrizzlyBears.class, LeoninScimitar.class, LiquimetalCoating.class})
class GutTrueSoulZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking may sacrifice another creature to create a tapped and attacking Skeleton")
    void sacrificesAnotherCreatureToCreateSkeleton() {
        Permanent gut = addCreatureReady(player1, new GutTrueSoulZealot());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertSkeletonWasCreated(gut);
    }

    @Test
    @DisplayName("Attacking may sacrifice an artifact to create a tapped and attacking Skeleton")
    void sacrificesArtifactToCreateSkeleton() {
        Permanent gut = addCreatureReady(player1, new GutTrueSoulZealot());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertSkeletonWasCreated(gut);
    }

    @Test
    @DisplayName("Declining the sacrifice does not create a Skeleton")
    void decliningSacrificeDoesNothing() {
        addCreatureReady(player1, new GutTrueSoulZealot());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The source cannot be sacrificed as another creature")
    void sourceCannotBeSacrificed() {
        Permanent gut = addCreatureReady(player1, new GutTrueSoulZealot());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gut);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Gut triggers when another creature attacks while Gut stays back")
    void triggersWithoutGutAttacking() {
        Permanent gut = harness.addToBattlefieldAndReturn(player1, new GutTrueSoulZealot());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertSkeletonWasCreated(gut);
    }

    @Test
    @DisplayName("Attacking with multiple creatures triggers Gut only once")
    void multipleAttackersCreateOnlyOneSkeleton() {
        Permanent gut = addCreatureReady(player1, new GutTrueSoulZealot());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertSkeletonWasCreated(gut);
    }

    @Test
    @DisplayName("An opponent attacking does not trigger Gut")
    void opponentsAttackDoesNotTriggerGut() {
        Permanent gut = addCreatureReady(player1, new GutTrueSoulZealot());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(countPermanents(player1, "Skeleton")).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gut);
    }

    @Test
    @DisplayName("Gut can sacrifice itself if it has become an artifact")
    void artifactGutCanSacrificeItself() {
        Permanent gut = addCreatureReady(player1, new GutTrueSoulZealot());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, null, gut.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, gut.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gut);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gut.getCard());
        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
    }

    @Test
    @DisplayName("The Skeleton is created during the same resolution as the sacrifice")
    void sacrificeAndTokenCreationResolveTogether() {
        Permanent gut = addCreatureReady(player1, new GutTrueSoulZealot());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertSkeletonWasCreated(gut);
    }

    private void assertSkeletonWasCreated(Permanent gut) {
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getCard().getName()).isEqualTo("Skeleton");
                    assertThat(permanent.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(permanent.getCard().getPower()).isEqualTo(4);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(1);
                    assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.SKELETON);
                    assertThat(permanent.getCard().getKeywords()).contains(Keyword.MENACE);
                    assertThat(permanent.isTapped()).isTrue();
                    assertThat(permanent.isAttackedThisTurn()).isTrue();
                });
        assertThat(gut).isIn(gd.playerBattlefields.get(player1.getId()));
    }
}
