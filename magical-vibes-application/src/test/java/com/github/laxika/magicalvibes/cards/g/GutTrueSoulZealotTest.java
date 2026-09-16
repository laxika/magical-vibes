package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GutTrueSoulZealot.class, GrizzlyBears.class, LeoninScimitar.class})
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
