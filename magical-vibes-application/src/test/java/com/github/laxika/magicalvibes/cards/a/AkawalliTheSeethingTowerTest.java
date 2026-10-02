package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkawalliTheSeethingTower.class, GrizzlyBears.class, LightningBolt.class})
class AkawalliTheSeethingTowerTest extends BaseCardTest {

    @Test
    void getsFirstDescendBonusAtFourPermanentCards() {
        harness.setGraveyard(player1, permanentCards(4));
        Permanent akawalli = harness.addToBattlefieldAndReturn(player1, new AkawalliTheSeethingTower());

        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, akawalli)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, akawalli, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, akawalli)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void getsSecondDescendBonusAndBlockRestrictionAtEightPermanentCards() {
        harness.setGraveyard(player1, permanentCards(8));
        Permanent akawalli = harness.addToBattlefieldAndReturn(player1, new AkawalliTheSeethingTower());

        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, akawalli)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, akawalli, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, akawalli)).isEqualTo(1);
    }

    @Test
    void doesNotCountNonpermanentCardsOrOpponentsGraveyard() {
        List<Card> ownGraveyard = new ArrayList<>(permanentCards(3));
        ownGraveyard.add(new LightningBolt());
        harness.setGraveyard(player1, ownGraveyard);
        harness.setGraveyard(player2, permanentCards(8));
        Permanent akawalli = harness.addToBattlefieldAndReturn(player1, new AkawalliTheSeethingTower());

        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, akawalli)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, akawalli, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, akawalli)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void losesSecondDescendBonusWhenGraveyardDropsBelowEightPermanentCards() {
        harness.setGraveyard(player1, permanentCards(8));
        Permanent akawalli = harness.addToBattlefieldAndReturn(player1, new AkawalliTheSeethingTower());

        harness.setGraveyard(player1, permanentCards(4));

        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, akawalli)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, akawalli, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, akawalli)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void sevenPermanentCardsAndAnInstantDoNotEnableDescendEight() {
        List<Card> graveyard = new ArrayList<>(permanentCards(7));
        graveyard.add(new LightningBolt());
        harness.setGraveyard(player1, graveyard);
        Permanent akawalli = harness.addToBattlefieldAndReturn(player1, new AkawalliTheSeethingTower());

        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, akawalli)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, akawalli, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, akawalli)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void losesBothBonusesWhenGraveyardDropsBelowFourPermanentCards() {
        harness.setGraveyard(player1, permanentCards(8));
        Permanent akawalli = harness.addToBattlefieldAndReturn(player1, new AkawalliTheSeethingTower());

        harness.setGraveyard(player1, permanentCards(3));

        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, akawalli)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, akawalli, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, akawalli)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void gainsBonusesAsGraveyardGrowsWhileOnBattlefield() {
        Permanent akawalli = harness.addToBattlefieldAndReturn(player1, new AkawalliTheSeethingTower());
        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, akawalli, Keyword.TRAMPLE)).isFalse();

        harness.setGraveyard(player1, permanentCards(4));
        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, akawalli)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, akawalli, Keyword.TRAMPLE)).isTrue();

        harness.setGraveyard(player1, permanentCards(8));
        assertThat(gqs.getEffectivePower(gd, akawalli)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, akawalli)).isEqualTo(7);
        assertThat(gqs.getMaxBlockersAllowed(gd, akawalli)).isEqualTo(1);
    }

    @Test
    void rejectsTwoBlockersAtDescendEight() {
        harness.setGraveyard(player1, permanentCards(8));
        Permanent attacker = addCreatureReady(player1, new AkawalliTheSeethingTower());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    void permitsTwoBlockersBelowDescendEight() {
        harness.setGraveyard(player1, permanentCards(7));
        Permanent attacker = addCreatureReady(player1, new AkawalliTheSeethingTower());
        attacker.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    private List<Card> permanentCards(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new GrizzlyBears())
                .toList();
    }
}
