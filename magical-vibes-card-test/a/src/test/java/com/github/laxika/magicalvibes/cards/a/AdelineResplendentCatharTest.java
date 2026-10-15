package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NoviceOccultist;
import com.github.laxika.magicalvibes.cards.t.TeferiWhoSlowsTheSunset;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdelineResplendentCathar.class, GrizzlyBears.class, NoviceOccultist.class,
        TeferiWhoSlowsTheSunset.class})
class AdelineResplendentCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 4")
    void powerEqualsControlledCreatures() {
        Permanent adeline = addCreatureReady(player1, new AdelineResplendentCathar());

        assertThat(gqs.getEffectivePower(gd, adeline)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adeline)).isEqualTo(4);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, adeline)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adeline)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking creates a tapped and attacking Human token")
    void attackingCreatesHumanToken() {
        addCreatureReady(player1, new AdelineResplendentCathar());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());

        Permanent token = findPermanents(player1, "Human").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Attacking with another creature triggers Adeline even when she cannot attack")
    void anotherCreatureAttackingCreatesToken() {
        harness.addToBattlefield(player1, new AdelineResplendentCathar());
        addCreatureReady(player1, new NoviceOccultist());
        addCreatureReady(player2, new NoviceOccultist());

        declareAttackers(List.of(1));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        assertThat(findPermanent(player1, "Human").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Multiple attackers create only one token and the token increases Adeline's power")
    void multipleAttackersTriggerOnlyOnce() {
        Permanent adeline = addCreatureReady(player1, new AdelineResplendentCathar());
        addCreatureReady(player1, new NoviceOccultist());
        addCreatureReady(player2, new NoviceOccultist());

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        assertThat(adeline.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, adeline)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Human"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Human"))).isEqualTo(1);
    }

    @Test
    @DisplayName("The token may attack an opponent's planeswalker while Adeline attacks the player")
    void tokenCanAttackPlaneswalker() {
        addCreatureReady(player1, new AdelineResplendentCathar());
        addCreatureReady(player2, new NoviceOccultist());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TeferiWhoSlowsTheSunset());
        planeswalker.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY, 4);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());

        Permanent token = findPermanent(player1, "Human");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(planeswalker.getId());
    }

    @Test
    @DisplayName("An opponent attacking does not trigger Adeline")
    void opponentAttackDoesNotCreateToken() {
        harness.addToBattlefield(player1, new AdelineResplendentCathar());
        addCreatureReady(player1, new NoviceOccultist());
        addCreatureReady(player2, new NoviceOccultist());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Human")).isZero();
    }
}
