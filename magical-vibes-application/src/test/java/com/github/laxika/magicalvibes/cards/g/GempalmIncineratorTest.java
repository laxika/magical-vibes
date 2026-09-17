package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FreneticRaptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GempalmIncinerator.class, FreneticRaptor.class, GoblinTurncoat.class})
class GempalmIncineratorTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling may deal damage equal to all battlefield Goblins and draws")
    void cyclingDealsDamageEqualToAllBattlefieldGoblins() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreneticRaptor());
        harness.addToBattlefield(player1, new GoblinTurncoat());
        harness.addToBattlefield(player2, new GoblinTurncoat());
        prepareCycle();

        cycleAndChoose(target, true);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Frenetic Raptor");
        harness.assertInGraveyard(player1, "Gempalm Incinerator");
        harness.assertInHand(player1, "Goblin Turncoat");
    }

    @Test
    @DisplayName("Cycling may be declined")
    void cyclingMayBeDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreneticRaptor());
        harness.addToBattlefield(player1, new GoblinTurncoat());
        prepareCycle();

        cycleAndChoose(target, false);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Frenetic Raptor");
        harness.assertInGraveyard(player1, "Gempalm Incinerator");
        harness.assertInHand(player1, "Goblin Turncoat");
    }

    @Test
    @DisplayName("Cycling can deal zero damage when no Goblins are on the battlefield")
    void cyclingWithNoGoblinsCanStillTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreneticRaptor());
        prepareCycle();

        cycleAndChoose(target, true);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Frenetic Raptor");
        harness.assertInGraveyard(player1, "Gempalm Incinerator");
        harness.assertInHand(player1, "Goblin Turncoat");
    }

    private void prepareCycle() {
        harness.setHand(player1, List.of(new GempalmIncinerator()));
        harness.setLibrary(player1, List.of(new GoblinTurncoat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void cycleAndChoose(Permanent target, boolean accept) {
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
        harness.passBothPriorities();
    }
}
