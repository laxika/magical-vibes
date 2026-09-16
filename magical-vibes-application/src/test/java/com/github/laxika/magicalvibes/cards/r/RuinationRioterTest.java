package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinationRioter.class, Murder.class, Forest.class, LlanowarElves.class})
class RuinationRioterTest extends BaseCardTest {

    @Test
    @DisplayName("When Ruination Rioter dies, it may deal damage equal to lands in its controller's graveyard")
    void deathTriggerDealsDamageEqualToLandsInGraveyard() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        destroyRioter(rioter);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ruination Rioter counts lands but not other cards in its controller's graveyard")
    void deathTriggerCountsOnlyLands() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new LlanowarElves()));
        harness.setLife(player2, 20);
        destroyRioter(rioter);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining Ruination Rioter's death trigger deals no damage")
    void decliningDeathTriggerDealsNoDamage() {
        Permanent rioter = harness.addToBattlefieldAndReturn(player1, new RuinationRioter());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);
        destroyRioter(rioter);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void destroyRioter(Permanent rioter) {
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, rioter.getId());
        harness.passBothPriorities();
    }
}
