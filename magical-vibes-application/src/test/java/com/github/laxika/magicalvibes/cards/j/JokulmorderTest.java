package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Jokulmorder.class, SnowCoveredForest.class, SnowCoveredIsland.class, SnowCoveredMountain.class})
class JokulmorderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and sacrifices five lands when its controller accepts")
    void entersTappedAndSacrificesFiveLands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SnowCoveredForest());
        }

        castAndResolveJokulmorder();
        Permanent jokulmorder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Jokulmorder)
                .findFirst()
                .orElseThrow();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jokulmorder.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof SnowCoveredForest);
        harness.assertOnBattlefield(player1, "Jokulmorder");
    }

    @Test
    @DisplayName("Sacrifices exactly five lands when more than five are available")
    void sacrificesExactlyFiveLandsWhenMoreAreAvailable() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new SnowCoveredForest());
        }

        castAndResolveJokulmorder();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        List<UUID> landsToSacrifice = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SnowCoveredForest)
                .map(Permanent::getId)
                .limit(5)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, landsToSacrifice);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof SnowCoveredForest)
                .hasSize(1);
        harness.assertOnBattlefield(player1, "Jokulmorder");
    }

    @Test
    @DisplayName("Is sacrificed without sacrificing lands when its controller declines")
    void isSacrificedWhenControllerDeclinesToSacrificeLands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SnowCoveredForest());
        }

        castAndResolveJokulmorder();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Jokulmorder");
        harness.assertInGraveyard(player1, "Jokulmorder");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof SnowCoveredForest)
                .hasSize(5);
    }

    @Test
    @DisplayName("Is sacrificed when its controller cannot sacrifice five lands")
    void isSacrificedWithoutFiveLands() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SnowCoveredForest());
        }

        castAndResolveJokulmorder();

        harness.assertNotOnBattlefield(player1, "Jokulmorder");
        harness.assertInGraveyard(player1, "Jokulmorder");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(4);
    }

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent jokulmorder = harness.addToBattlefieldAndReturn(player1, new Jokulmorder());
        jokulmorder.tap();

        advanceToUpkeep(player1);

        assertThat(jokulmorder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May untap when its controller plays an Island")
    void mayUntapWhenControllerPlaysIsland() {
        Permanent jokulmorder = harness.addToBattlefieldAndReturn(player1, new Jokulmorder());
        jokulmorder.tap();
        harness.setHand(player1, List.of(new SnowCoveredIsland()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jokulmorder.isTapped()).isFalse();
    }

    @Test
    @DisplayName("May decline to untap when its controller plays an Island")
    void mayDeclineToUntapWhenControllerPlaysIsland() {
        Permanent jokulmorder = harness.addToBattlefieldAndReturn(player1, new Jokulmorder());
        jokulmorder.tap();
        harness.setHand(player1, List.of(new SnowCoveredIsland()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(jokulmorder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent plays an Island")
    void doesNotTriggerWhenOpponentPlaysIsland() {
        Permanent jokulmorder = harness.addToBattlefieldAndReturn(player1, new Jokulmorder());
        jokulmorder.tap();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SnowCoveredIsland()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(jokulmorder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when its controller plays a non-Island land")
    void doesNotTriggerForNonIslandLand() {
        Permanent jokulmorder = harness.addToBattlefieldAndReturn(player1, new Jokulmorder());
        jokulmorder.tap();
        harness.setHand(player1, List.of(new SnowCoveredMountain()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(jokulmorder.isTapped()).isTrue();
    }

    private void castAndResolveJokulmorder() {
        harness.castFromHand(player1, new Jokulmorder(), "{4}{U}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
