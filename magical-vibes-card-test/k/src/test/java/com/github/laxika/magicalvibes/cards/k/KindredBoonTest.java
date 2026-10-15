package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KindredBoon.class, GrizzlyBears.class, HillGiant.class, Naturalize.class})
class KindredBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type enables divinity counters on matching creatures")
    void putsDivinityCounterOnChosenTypeCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.castFromHand(player1, new KindredBoon(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardSubtype.BEAR.name());

        Permanent boon = findPermanent(player1, "Kindred Boon");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(boon),
                0,
                null,
                bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.DIVINITY)).isOne();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(giant.getCounterCount(CounterType.DIVINITY)).isZero();
    }

    @Test
    @DisplayName("The activated ability cannot target a creature outside the chosen type")
    void rejectsCreatureOutsideChosenType() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new KindredBoon());
        boon.setChosenSubtype(CardSubtype.BEAR);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(boon),
                0,
                null,
                giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("chosen type");
    }

    @Test
    @DisplayName("Creatures you control with divinity counters have indestructible")
    void grantsIndestructibleToOwnDivinityCreatures() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new KindredBoon());
        boon.setChosenSubtype(CardSubtype.BEAR);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        ownBear.setCounterCount(CounterType.DIVINITY, 1);
        ownGiant.setCounterCount(CounterType.DIVINITY, 1);
        opponentBear.setCounterCount(CounterType.DIVINITY, 1);

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownGiant, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.INDESTRUCTIBLE)).isFalse();

        ownBear.setCounterCount(CounterType.DIVINITY, 0);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target an opponent's creature of the chosen type")
    void rejectsOpponentCreatureOfChosenType() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new KindredBoon());
        boon.setChosenSubtype(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(boon), 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bear.getCounterCount(CounterType.DIVINITY)).isZero();
    }

    @Test
    @DisplayName("The ability can be activated repeatedly without tapping Kindred Boon")
    void repeatedActivationsAccumulateDivinityCounters() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new KindredBoon());
        boon.setChosenSubtype(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        for (int activation = 0; activation < 2; activation++) {
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(boon), 0, null, bear.getId());
            harness.passBothPriorities();
        }

        assertThat(bear.getCounterCount(CounterType.DIVINITY)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A target that changes controller before resolution receives no counter")
    void targetMustStillBeControlledOnResolution() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new KindredBoon());
        boon.setChosenSubtype(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(boon), 0, null, bear.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bear);
        gd.playerBattlefields.get(player2.getId()).add(bear);
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Divinity counters do not make noncreature permanents indestructible")
    void doesNotProtectNoncreatureWithDivinityCounter() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new KindredBoon());
        boon.setChosenSubtype(CardSubtype.BEAR);
        boon.setCounterCount(CounterType.DIVINITY, 1);

        assertThat(gqs.hasKeyword(gd, boon, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing Kindred Boon in response does not stop its counter ability")
    void abilityResolvesAfterBoonIsDestroyed() {
        Permanent boon = harness.addToBattlefieldAndReturn(player1, new KindredBoon());
        boon.setChosenSubtype(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.DIVINITY, 1);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(boon), 0, null, bear.getId());

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, boon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kindred Boon");
        assertThat(bear.getCounterCount(CounterType.DIVINITY)).isOne();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.DIVINITY)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
