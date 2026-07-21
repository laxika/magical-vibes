package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SandStranglerTest extends BaseCardTest {

    @Test
    @DisplayName("With a Desert on the battlefield, ETB may deal 3 damage to target creature")
    void etbDamagesWithDesertOnBattlefield() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSandStrangler();
        harness.passBothPriorities(); // resolve creature -> target prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve ETB -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("With a Desert in the graveyard, ETB may deal 3 damage to target creature")
    void etbDamagesWithDesertInGraveyard() {
        harness.setGraveyard(player1, List.of(new SunscorchedDesert()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Declining the may deals no damage")
    void decliningMayDealsNoDamage() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new SunscorchedDesert()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSandStrangler();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Without any Desert, the ETB does not trigger")
    void noDesertDoesNotTrigger() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSandStrangler();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sand Strangler"));
    }

    private void castSandStrangler() {
        harness.setHand(player1, List.of(new SandStrangler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
