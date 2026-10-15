package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BorderPatrol;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorderPatrol.class, GrizzlyBears.class, Island.class, TropicalIsland.class, Wonder.class})
class WonderTest extends BaseCardTest {

    @Test
    @DisplayName("A Wonder in the graveyard gives your creatures flying while you control an Island")
    void grantsFlyingFromGraveyardWithIsland() {
        harness.setGraveyard(player1, List.of(new Wonder()));
        harness.addToBattlefield(player1, new Island());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BorderPatrol());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wonder's graveyard ability turns off without an Island or after Wonder leaves the graveyard")
    void graveyardAbilityTurnsOffWhenConditionChanges() {
        Wonder wonder = new Wonder();
        harness.setGraveyard(player1, List.of(wonder));
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();

        Island island = new Island();
        harness.addToBattlefield(player1, island);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() == island);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wonder checks for an Island controlled by the graveyard card's controller")
    void islandMustBeControlledByGraveyardCardController() {
        harness.setGraveyard(player2, List.of(new Wonder()));
        harness.addToBattlefield(player1, new Island());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new BorderPatrol());

        assertThat(gqs.hasKeyword(gd, player1Creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, player2Creature, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.hasKeyword(gd, player1Creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, player2Creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Wonder requires its controller to control the Island")
    void requiresIslandControlledByWonderController() {
        harness.setGraveyard(player1, List.of(new Wonder()));
        harness.addToBattlefield(player2, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A land with the Island subtype satisfies Wonder's condition")
    void recognizesIslandSubtype() {
        harness.setGraveyard(player1, List.of(new Wonder()));
        harness.addToBattlefield(player1, new TropicalIsland());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A Wonder in an opponent's graveyard uses that opponent's Island condition")
    void usesControllerOfWonderInGraveyard() {
        harness.setGraveyard(player2, List.of(new Wonder()));
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wonder stops granting flying when it leaves the graveyard even if the Island remains")
    void stopsGrantingFlyingAfterLeavingGraveyardWithIslandStillPresent() {
        Wonder wonder = new Wonder();
        harness.setGraveyard(player1, List.of(wonder));
        harness.addToBattlefield(player1, new Island());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(wonder));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Wonder on the battlefield does not grant flying to other creatures")
    void doesNotGrantFlyingFromBattlefield() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Wonder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("One remaining Wonder continues granting flying, including to newly entering creatures")
    void remainingWonderGrantsFlyingToExistingAndNewCreatures() {
        Wonder first = new Wonder();
        Wonder second = new Wonder();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addToBattlefield(player1, new Island());
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of(second));
        harness.setExile(player1, List.of(first));
        Permanent entering = harness.addToBattlefieldAndReturn(player1, new BorderPatrol());

        assertThat(gqs.hasKeyword(gd, existing, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, entering, Keyword.FLYING)).isTrue();

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(second));

        assertThat(gqs.hasKeyword(gd, existing, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, entering, Keyword.FLYING)).isFalse();
    }
}
