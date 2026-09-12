package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({HuntingMoa.class, PlatedSpider.class, YavimayaHollow.class})
class HuntingMoaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());

        harness.setHand(player1, List.of(new HuntingMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, 0, spider.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger puts a +1/+1 counter on target creature")
    void deathPutsCounterOnTargetCreature() {
        Permanent moa = harness.addToBattlefieldAndReturn(player1, new HuntingMoa());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new PlatedSpider());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, moa));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Echo {2}{G} sacrifices Hunting Moa when unpaid")
    void unpaidEchoSacrificesMoa() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        harness.setHand(player1, List.of(new HuntingMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, 0, spider.getId());
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
    @DisplayName("Paying {2}{G} for echo keeps Hunting Moa and echo does not trigger again")
    void paidEchoKeepsMoaAndIsOneShot() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        harness.setHand(player1, List.of(new HuntingMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, 0, spider.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Hunting Moa");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Hunting Moa");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaHollow());

        harness.setHand(player1, List.of(new HuntingMoa()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
