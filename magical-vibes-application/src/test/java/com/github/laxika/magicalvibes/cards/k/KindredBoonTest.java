package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({KindredBoon.class, GrizzlyBears.class, HillGiant.class})
class KindredBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type enables divinity counters on matching creatures")
    void putsDivinityCounterOnChosenTypeCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new KindredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
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
}
