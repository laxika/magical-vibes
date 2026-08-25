package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RebornHero.class, GrizzlyBears.class})
class RebornHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when threshold is not met before Reborn Hero dies")
    void doesNotTriggerWithoutThresholdBeforeDeath() {
        RebornHero hero = new RebornHero();
        harness.setGraveyard(player1, graveyardWithSevenOrFewerCards(6));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, hero);

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Reborn Hero");
    }

    @Test
    @DisplayName("Triggers when threshold is met before Reborn Hero dies")
    void triggersWithThresholdBeforeDeath() {
        harness.setGraveyard(player1, graveyardWithSevenOrFewerCards(7));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new RebornHero());

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{W}{W}");
    }

    @Test
    @DisplayName("Paying {W}{W} returns Reborn Hero to the battlefield under its controller's control")
    void payingManaReturnsHeroToBattlefield() {
        RebornHero hero = new RebornHero();
        harness.setGraveyard(player1, graveyardWithSevenOrFewerCards(7));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, hero);
        harness.addMana(player1, ManaColor.WHITE, 2);

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(hero.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(hero.getId()));
    }

    @Test
    @DisplayName("Declining to pay keeps Reborn Hero in the graveyard")
    void decliningManaKeepsHeroInGraveyard() {
        RebornHero hero = new RebornHero();
        harness.setGraveyard(player1, graveyardWithSevenOrFewerCards(7));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, hero);

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(hero.getId()));
        harness.assertNotOnBattlefield(player1, "Reborn Hero");
    }

    private List<Card> graveyardWithSevenOrFewerCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> (Card) new GrizzlyBears())
                .toList();
    }
}
