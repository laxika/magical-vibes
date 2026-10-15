package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HordewingSkaab.class, Gravedigger.class, GrizzlyBears.class, Forest.class})
class HordewingSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Other Zombies you control gain flying")
    void grantsFlyingToOtherZombiesYouControl() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new HordewingSkaab());

        Permanent zombie = findPermanent(player1, "Gravedigger");

        assertThat(gqs.hasKeyword(gd, zombie, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Non-Zombies do not gain flying")
    void doesNotGrantFlyingToNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HordewingSkaab());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Combat damage may draw a card and then discard a card")
    void mayDrawThenDiscardAfterZombieDealsCombatDamage() {
        Permanent skaab = addCreatureReady(player1, new HordewingSkaab());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        skaab.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(GrizzlyBears.class);
    }

    @Test
    void doesNotGrantFlyingToOpponentsZombies() {
        harness.addToBattlefield(player2, new Gravedigger());
        harness.addToBattlefield(player1, new HordewingSkaab());

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Gravedigger"), Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingGrantEndsWhenSkaabLeavesBattlefield() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new HordewingSkaab());
        Permanent zombie = findPermanent(player1, "Gravedigger");
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Hordewing Skaab"));

        assertThat(gqs.hasKeyword(gd, zombie, Keyword.FLYING)).isFalse();
    }

    @Test
    void decliningDrawDoesNotDiscard() {
        Permanent zombie = addCreatureReady(player1, new Gravedigger());
        harness.addToBattlefield(player1, new HordewingSkaab());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        zombie.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(GrizzlyBears.class);
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void multipleZombiesHittingOneOpponentTriggerOnlyOnce() {
        Permanent skaab = addCreatureReady(player1, new HordewingSkaab());
        Permanent zombie = addCreatureReady(player1, new Gravedigger());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        skaab.setAttacking(true);
        zombie.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(GrizzlyBears.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void nonZombieCombatDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new HordewingSkaab());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        bears.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

}
