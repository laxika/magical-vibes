package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShattergangBrothers.class, GrizzlyBears.class, FountainOfYouth.class, Pacifism.class})
class ShattergangBrothersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice a creature ability makes each opponent sacrifice a creature")
    void sacrificesCreatureForCreatureEdict() {
        addSource();
        Permanent sacrificedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        addMana(player1, ManaColor.BLACK);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificedCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifice an artifact ability makes each opponent sacrifice an artifact")
    void sacrificesArtifactForArtifactEdict() {
        addSource();
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());

        addMana(player1, ManaColor.RED);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Sacrifice an enchantment ability makes each opponent sacrifice an enchantment")
    void sacrificesEnchantmentForEnchantmentEdict() {
        addSource();
        harness.addToBattlefield(player1, new Pacifism());
        harness.addToBattlefield(player2, new Pacifism());

        addMana(player1, ManaColor.GREEN);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertNotOnBattlefield(player2, "Pacifism");
    }

    private void addSource() {
        harness.addToBattlefield(player1, new ShattergangBrothers());
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player, ManaColor color) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, color, 1);
    }
}
