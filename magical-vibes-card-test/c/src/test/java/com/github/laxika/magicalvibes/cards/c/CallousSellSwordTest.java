package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.b.BurnTogether;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallousSellSword.class, BurnTogether.class, GrizzlyBears.class, Shock.class,
        InvasionOfZendikar.class, AwakenedSkyclave.class})
class CallousSellSwordTest extends BaseCardTest {

    @Test
    void entersWithCountersForCreaturesThatDiedUnderYourControlThisTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        CallousSellSword card = new CallousSellSword();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sellSword = findPermanent(player1, "Callous Sell-Sword");
        assertThat(sellSword.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adventureDealsPowerDamageToAnotherTargetThenSacrificesTheSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CallousSellSword card = new CallousSellSword();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, List.of(source.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureRequiresTheDamageTargetToBeDifferentFromTheSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CallousSellSword card = new CallousSellSword();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersWithoutCountersWhenNoCreaturesDied() {
        harness.setHand(player1, List.of(new CallousSellSword()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Callous Sell-Sword")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsMultipleControlledDeathsButNotAnOpponentsDeath() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CallousSellSword());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CallousSellSword());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CallousSellSword());
        harness.setHand(player1, List.of(new CallousSellSword(), new CallousSellSword()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, List.of(first.getId(), opponent.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, List.of(second.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new CallousSellSword()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Callous Sell-Sword")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canCastCreatureFromAdventureExileAndCountTheSacrificedCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CallousSellSword());
        CallousSellSword card = new CallousSellSword();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, List.of(source.getId(), player2.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Callous Sell-Sword")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureUsesTheSourcesCurrentPower() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CallousSellSword());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new CallousSellSword()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, List.of(source.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
    }

    @Test
    void adventureCannotUseAnOpponentsCreatureAsItsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new CallousSellSword());
        harness.setHand(player1, List.of(new CallousSellSword()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0,
                List.of(source.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureStillSacrificesTheSourceWhenTheDamageTargetLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CallousSellSword());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new CallousSellSword());
        CallousSellSword card = new CallousSellSword();
        harness.setHand(player1, List.of(card, new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAdventure(player1, 0, List.of(source.getId(), victim.getId()));
        harness.castAndResolveInstant(player1, 0, victim.getId());

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureDealsNoDamageWhenTheSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CallousSellSword());
        CallousSellSword card = new CallousSellSword();
        harness.setHand(player1, List.of(card, new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAdventure(player1, 0, List.of(source.getId(), player2.getId()));
        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureGoesToGraveyardWhenBothTargetsLeave() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CallousSellSword());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new CallousSellSword());
        CallousSellSword card = new CallousSellSword();
        harness.setHand(player1, List.of(card, new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAdventure(player1, 0, List.of(source.getId(), victim.getId()));
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.castAndResolveInstant(player1, 0, victim.getId());

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCanDamageABattle() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CallousSellSword());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());
        harness.setHand(player1, List.of(new CallousSellSword()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, List.of(source.getId(), battle.getId()));
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
    }
}
