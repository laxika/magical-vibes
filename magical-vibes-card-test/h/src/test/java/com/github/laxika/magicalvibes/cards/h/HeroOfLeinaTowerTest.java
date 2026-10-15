package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.RenownedWeaponsmith;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroOfLeinaTower.class, GiantGrowth.class, Shock.class, RenownedWeaponsmith.class})
class HeroOfLeinaTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying X for a spell that targets Hero of Leina Tower puts X +1/+1 counters on it")
    void payingXAddsCounters() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID heroId = harness.getPermanentId(player1, "Hero of Leina Tower");
        harness.castAndResolveInstant(player1, 0, heroId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        Permanent hero = findPermanent(player1, "Hero of Leina Tower");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing X=0 for Hero of Leina Tower's heroic ability does nothing")
    void choosingZeroDoesNothing() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID heroId = harness.getPermanentId(player1, "Hero of Leina Tower");
        harness.castAndResolveInstant(player1, 0, heroId);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        Permanent hero = findPermanent(player1, "Hero of Leina Tower");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger Hero of Leina Tower")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent hero = findPermanent(player1, "Hero of Leina Tower");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Hero of Leina Tower"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
        assertThat(findPermanent(player1, "Hero of Leina Tower")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void targetingAnotherCreatureDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.addToBattlefield(player2, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Hero of Leina Tower"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
        assertThat(findPermanent(player1, "Hero of Leina Tower")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Hero of Leina Tower")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void noAvailableManaDoesNotPreventTargetingSpellFromResolving() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Hero of Leina Tower"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
        assertThat(findPermanent(player1, "Hero of Leina Tower")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    void heroicTriggerSurvivesSourceBeingDestroyed() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.RED, 1);
        UUID heroId = harness.getPermanentId(player1, "Hero of Leina Tower");

        harness.castInstant(player1, 0, heroId);
        harness.castAndResolveInstant(player2, 0, heroId);
        harness.assertInGraveyard(player1, "Hero of Leina Tower");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Hero of Leina Tower");
    }

    @Test
    void artifactRestrictedManaCannotIncreaseHeroicPayment() {
        harness.addToBattlefield(player1, new HeroOfLeinaTower());
        addCreatureReady(player1, new RenownedWeaponsmith());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Hero of Leina Tower"));
        harness.passBothPriorities();
        PendingInteraction.XValueChoice choice = gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        harness.handleXValueChosen(player1, 1);

        assertThat(findPermanent(player1, "Hero of Leina Tower")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
        assertThat(choice.maxValue()).isEqualTo(1);
    }
}
