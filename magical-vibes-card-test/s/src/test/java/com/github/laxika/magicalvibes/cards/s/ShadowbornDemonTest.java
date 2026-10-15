package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowbornDemon.class, ChildOfNight.class, GiantSpider.class, Swamp.class})
class ShadowbornDemonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target non-Demon creature")
    void etbDestroysTargetCreature() {
        harness.addToBattlefield(player2, new ChildOfNight());
        harness.setHand(player1, List.of(new ShadowbornDemon()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Child of Night");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        harness.assertNotOnBattlefield(player2, "Child of Night");
        harness.assertInGraveyard(player2, "Child of Night");
    }

    @Test
    @DisplayName("Cannot target a Demon")
    void cannotTargetDemon() {
        harness.addToBattlefield(player2, new ShadowbornDemon());
        harness.setHand(player1, List.of(new ShadowbornDemon()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Shadowborn Demon");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Demon creature");
    }

    @Test
    @DisplayName("Upkeep: with fewer than six creature cards in the graveyard, controller sacrifices a creature")
    void upkeepSacrificesWithSmallGraveyard() {
        harness.addToBattlefield(player1, new ShadowbornDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ChildOfNight());
        harness.setGraveyard(player1, List.of(new GiantSpider()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertOnBattlefield(player1, "Shadowborn Demon");
    }

    @Test
    @DisplayName("Upkeep: it may sacrifice itself when it is the only creature")
    void upkeepCanSacrificeItself() {
        harness.addToBattlefield(player1, new ShadowbornDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shadowborn Demon");
        harness.assertInGraveyard(player1, "Shadowborn Demon");
    }

    @Test
    @DisplayName("Upkeep: no sacrifice with six creature cards in the graveyard")
    void upkeepNoSacrificeWithSixCreatureCards() {
        harness.addToBattlefield(player1, new ShadowbornDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ChildOfNight());
        harness.setGraveyard(player1, List.of(
                new GiantSpider(), new GiantSpider(), new GiantSpider(),
                new ChildOfNight(), new ChildOfNight(), new ChildOfNight()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new ShadowbornDemon());
        harness.addToBattlefield(player1, new ChildOfNight());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        harness.assertOnBattlefield(player1, "Child of Night");
    }

    @Test
    void etbCanDestroyControllersOwnCreature() {
        harness.addToBattlefield(player1, new ChildOfNight());
        harness.setHand(player1, List.of(new ShadowbornDemon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Child of Night"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        harness.assertNotOnBattlefield(player1, "Child of Night");
        harness.assertInGraveyard(player1, "Child of Night");
    }

    @Test
    void canEnterWithoutAnyLegalEtbTarget() {
        harness.setHand(player1, List.of(new ShadowbornDemon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new ShadowbornDemon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                harness.getPermanentId(player2, "Swamp")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Demon creature");
    }

    @Test
    void upkeepCountsOnlyCreatureCardsInControllersGraveyard() {
        harness.addToBattlefield(player1, new ShadowbornDemon());
        harness.setGraveyard(player1, List.of(
                new GiantSpider(), new GiantSpider(), new GiantSpider(),
                new ChildOfNight(), new ChildOfNight(), new Swamp()));
        harness.setGraveyard(player2, List.of(
                new GiantSpider(), new GiantSpider(), new GiantSpider(),
                new ChildOfNight(), new ChildOfNight(), new ChildOfNight()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shadowborn Demon");
        harness.assertInGraveyard(player1, "Shadowborn Demon");
    }

    @Test
    void upkeepDoesNothingIfSixthCreatureCardArrivesBeforeResolution() {
        harness.addToBattlefield(player1, new ShadowbornDemon());
        harness.setGraveyard(player1, List.of(
                new GiantSpider(), new GiantSpider(), new GiantSpider(),
                new ChildOfNight(), new ChildOfNight()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(
                new GiantSpider(), new GiantSpider(), new GiantSpider(),
                new ChildOfNight(), new ChildOfNight(), new ChildOfNight()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        harness.assertNotInGraveyard(player1, "Shadowborn Demon");
    }

    @Test
    void upkeepDoesNotTriggerIfConditionOnlyBecomesTrueLater() {
        harness.addToBattlefield(player1, new ShadowbornDemon());
        harness.setGraveyard(player1, List.of(
                new GiantSpider(), new GiantSpider(), new GiantSpider(),
                new ChildOfNight(), new ChildOfNight(), new ChildOfNight()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.setGraveyard(player1, List.of(
                new GiantSpider(), new GiantSpider(), new GiantSpider(),
                new ChildOfNight(), new ChildOfNight()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        harness.assertNotInGraveyard(player1, "Shadowborn Demon");
    }
}
