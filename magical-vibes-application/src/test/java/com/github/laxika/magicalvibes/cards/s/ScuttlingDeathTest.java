package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScuttlingDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability gives target creature -1/-1 and sacrifices Scuttling Death")
    void sacAbilityGivesMinusOneMinusOne() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scuttling Death");
        harness.assertInGraveyard(player1, "Scuttling Death");

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("-1/-1 wears off at end of turn")
    void minusOneWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Soulshift 4 returns a targeted Spirit with mana value 4 or less when Scuttling Death dies")
    void soulshiftReturnsCheapSpirit() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift offers no choice with no Spirit in your graveyard")
    void soulshiftNoLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
