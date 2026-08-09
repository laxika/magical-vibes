package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuntingMoaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new HuntingMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger puts a +1/+1 counter on target creature")
    void deathPutsCounterOnTargetCreature() {
        Permanent moa = harness.addToBattlefieldAndReturn(player1, new HuntingMoa());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, moa));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Echo {2}{G} sacrifices Hunting Moa when unpaid")
    void unpaidEchoSacrificesMoa() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntingMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Hunting Moa");
        harness.assertInGraveyard(player1, "Hunting Moa");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new HuntingMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
