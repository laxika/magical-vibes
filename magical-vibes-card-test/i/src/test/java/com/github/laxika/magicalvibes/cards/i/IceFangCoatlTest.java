package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceFangCoatl.class, GrizzlyBears.class, SnowCoveredPlains.class})
class IceFangCoatlTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card")
    void etbDrawsACard() {
        harness.setLibrary(player1, List.of(new SnowCoveredPlains()));
        harness.setHand(player1, List.of(new IceFangCoatl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        int handAfterCast = gd.playerHands.get(player1.getId()).size();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterCast + 1);
    }

    @Test
    @DisplayName("Has deathtouch with at least three other snow permanents")
    void hasDeathtouchWithThreeOtherSnowPermanents() {
        Permanent coatl = addCoatl();
        addSnowPermanents(3);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Does not count itself or nonsnow permanents toward deathtouch")
    void doesNotCountItselfOrNonsnowPermanents() {
        Permanent coatl = addCoatl();
        addSnowPermanents(2);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Opponent's snow permanents do not enable deathtouch")
    void opponentSnowPermanentsDoNotCount() {
        Permanent coatl = addCoatl();
        addSnowPermanents(player2, 3);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Deathtouch is removed when the snow permanent threshold is no longer met")
    void deathtouchTracksSnowPermanents() {
        Permanent coatl = addCoatl();
        addSnowPermanents(3);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(1);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat")
    void canCastDuringOpponentsCombat() {
        harness.setLibrary(player1, List.of(new SnowCoveredPlains()));
        harness.setHand(player1, List.of(new IceFangCoatl()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ice-Fang Coatl");
        harness.assertInHand(player1, "Snow-Covered Plains");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Another Ice-Fang Coatl counts as an other snow permanent")
    void otherSnowCreatureCounts() {
        Permanent first = addCoatl();
        Permanent second = addCoatl();
        addSnowPermanents(2);

        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The enter trigger still draws after Coatl leaves the battlefield")
    void enterTriggerDrawsAfterSourceLeaves() {
        harness.setLibrary(player1, List.of(new SnowCoveredPlains()));
        harness.setHand(player1, List.of(new IceFangCoatl()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent coatl = findPermanent(player1, "Ice-Fang Coatl");
        assertThat(coatl).isNotNull();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(coatl);
        gd.playerGraveyards.get(player1.getId()).add(coatl.getCard());
        resolveAllTriggers();

        harness.assertInHand(player1, "Snow-Covered Plains");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Flying prevents a creature without flying or reach from blocking")
    void groundCreatureCannotBlock() {
        addCoatl();
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Deathtouch kills a creature with more toughness than Coatl's power")
    void deathtouchKillsLargerAttacker() {
        addCreatureReady(player2, new GrizzlyBears());
        addCoatl();
        addSnowPermanents(3);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ice-Fang Coatl");
    }

    private Permanent addCoatl() {
        return addCreatureReady(player1, new IceFangCoatl());
    }

    private void addSnowPermanents(int count) {
        addSnowPermanents(player1, count);
    }

    private void addSnowPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SnowCoveredPlains());
        }
    }
}
