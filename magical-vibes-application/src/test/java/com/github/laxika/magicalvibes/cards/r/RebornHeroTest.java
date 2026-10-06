package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChainerDementiaMaster;
import com.github.laxika.magicalvibes.cards.p.PitchstoneWall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RebornHero.class, PitchstoneWall.class, ChainerDementiaMaster.class})
class RebornHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when threshold is not met before Reborn Hero dies")
    void doesNotTriggerWithoutThresholdBeforeDeath() {
        RebornHero hero = new RebornHero();
        harness.setGraveyard(player1, graveyardWithCards(6));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, hero);

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Reborn Hero");
    }

    @Test
    @DisplayName("Triggers when threshold is met before Reborn Hero dies")
    void triggersWithThresholdBeforeDeath() {
        harness.setGraveyard(player1, graveyardWithCards(7));
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
        harness.setGraveyard(player1, graveyardWithCards(7));
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
        harness.setGraveyard(player1, graveyardWithCards(7));
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, hero);

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(hero.getId()));
        harness.assertNotOnBattlefield(player1, "Reborn Hero");
    }

    @Test
    @DisplayName("Paying returns only the Reborn Hero that died")
    void payingManaReturnsOnlyDyingHero() {
        RebornHero hero = new RebornHero();
        RebornHero otherHero = new RebornHero();
        List<Card> graveyard = new ArrayList<>(graveyardWithCards(7));
        graveyard.add(otherHero);
        harness.setGraveyard(player1, graveyard);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, hero);
        harness.addMana(player1, ManaColor.WHITE, 2);

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(hero.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(otherHero.getId()));
    }

    @Test
    @DisplayName("A simultaneous death cannot give Reborn Hero threshold retroactively")
    void simultaneousDeathDoesNotGrantThreshold() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new RebornHero());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new PitchstoneWall());

        hero.setMarkedDamage(2);
        wall.setMarkedDamage(5);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        harness.assertInGraveyard(player1, "Reborn Hero");
        harness.assertInGraveyard(player1, "Pitchstone Wall");
    }

    @Test
    @DisplayName("Losing threshold after death does not stop the return ability")
    void losingThresholdAfterDeathDoesNotStopReturn() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        RebornHero hero = new RebornHero();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, hero);
        harness.addMana(player1, ManaColor.WHITE, 2);

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.setGraveyard(player1, List.of(hero));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Reborn Hero");
        harness.assertNotInGraveyard(player1, "Reborn Hero");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("An opponent-owned Hero returns from its owner's graveyard under its last controller's control")
    void returnsOpponentOwnedHeroUnderLastControllersControl() {
        harness.addToBattlefield(player1, new ChainerDementiaMaster());
        harness.setGraveyard(player1, graveyardWithCards(7));
        RebornHero hero = new RebornHero();
        hero.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(hero));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, hero.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        Permanent permanent = findPermanent(player1, "Reborn Hero");
        permanent.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Reborn Hero");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Reborn Hero");
        harness.assertNotOnBattlefield(player2, "Reborn Hero");
        harness.assertNotInGraveyard(player2, "Reborn Hero");
    }

    @Test
    @DisplayName("An old death trigger cannot return Hero after it leaves the graveyard and dies again")
    void oldDeathTriggerCannotReturnNewGraveyardObject() {
        harness.addToBattlefield(player1, new ChainerDementiaMaster());
        harness.setGraveyard(player1, graveyardWithCards(7));
        RebornHero hero = new RebornHero();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, hero);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        permanent.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.activateAbility(player1, 0, null, hero.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Reborn Hero");
        returned.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Reborn Hero");
        harness.assertInGraveyard(player1, "Reborn Hero");
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> (Card) new PitchstoneWall())
                .toList();
    }
}
