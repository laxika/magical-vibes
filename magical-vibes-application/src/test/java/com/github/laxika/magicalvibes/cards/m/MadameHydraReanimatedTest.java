package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MadameHydraReanimated.class, Forest.class, GrizzlyBears.class})
class MadameHydraReanimatedTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each creature card in its controller's graveyard")
    void boostsForCreatureCardsInOwnGraveyard() {
        Permanent hydra = addCreatureReady(player1, new MadameHydraReanimated());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enters by milling two cards")
    void entersAndMillsTwoCards() {
        List<Card> milledCards = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, milledCards);

        harness.enterBattlefieldAndReturn(player1, new MadameHydraReanimated());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milledCards);
    }

    @Test
    @DisplayName("Attacking mills two cards")
    void attackMillsTwoCards() {
        List<Card> milledCards = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, milledCards);
        addCreatureReady(player1, new MadameHydraReanimated());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milledCards);
    }

    @Test
    void millingCreatureCardsUpdatesBoostImmediately() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card remainingCard = new Forest();
        harness.setLibrary(player1, List.of(creature, land, remainingCard));
        harness.setGraveyard(player1, List.of());
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new MadameHydraReanimated());

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, land);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(land));
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(2);
    }

    @Test
    void millsOnlyAvailableCardFromShortLibrary() {
        Card lastCard = new Forest();
        harness.setLibrary(player1, List.of(lastCard));
        harness.setGraveyard(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new MadameHydraReanimated());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lastCard);
    }

    @Test
    void enterTriggerStillMillsAfterSourceLeavesBattlefield() {
        List<Card> milledCards = List.of(new Forest(), new Forest());
        harness.setLibrary(player1, milledCards);
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new MadameHydraReanimated());
        gd.playerBattlefields.get(player1.getId()).remove(hydra);
        gd.playerGraveyards.get(player1.getId()).add(hydra.getCard());

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(milledCards);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void menaceRejectsSingleBlocker() {
        Permanent hydra = addCreatureReady(player1, new MadameHydraReanimated());
        hydra.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        Permanent hydra = addCreatureReady(player1, new MadameHydraReanimated());
        hydra.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }
}
