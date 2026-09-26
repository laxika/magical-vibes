package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RiverKaijin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurrGrafter.class, DevotedRetainer.class, LanternKami.class, RiverKaijin.class})
class BurrGrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability gives target creature +2/+2 and sacrifices Burr Grafter")
    void sacAbilityGivesPlusTwoPlusTwo() {
        harness.addToBattlefield(player1, new BurrGrafter());
        harness.addToBattlefield(player2, new DevotedRetainer());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Devoted Retainer"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Burr Grafter");
        harness.assertInGraveyard(player1, "Burr Grafter");

        Permanent target = findPermanent(player2, "Devoted Retainer");
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("+2/+2 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new BurrGrafter());
        harness.addToBattlefield(player2, new DevotedRetainer());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Devoted Retainer"));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Devoted Retainer");
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Soulshift 3 returns a targeted Spirit with mana value 3 or less when Burr Grafter dies")
    void soulshiftReturnsCheapSpirit() {
        harness.addToBattlefield(player1, new BurrGrafter());
        harness.addToBattlefield(player2, new DevotedRetainer());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Devoted Retainer"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift only targets Spirits with mana value 3 or less in its controller's graveyard")
    void soulshiftFiltersBySpiritManaValueAndController() {
        harness.addToBattlefield(player1, new BurrGrafter());
        harness.addToBattlefield(player2, new DevotedRetainer());
        Card eligibleSpirit = new RiverKaijin();
        Card expensiveSpirit = new BurrGrafter();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(eligibleSpirit, expensiveSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Devoted Retainer"));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligibleSpirit.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligibleSpirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(eligibleSpirit);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(eligibleSpirit);
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftMayBeDeclined() {
        harness.addToBattlefield(player1, new BurrGrafter());
        harness.addToBattlefield(player2, new DevotedRetainer());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Devoted Retainer"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(spirit);
    }

    @Test
    @DisplayName("Soulshift offers no choice with no Spirit in your graveyard")
    void soulshiftNoLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new BurrGrafter());
        harness.addToBattlefield(player2, new DevotedRetainer());
        Card nonSpirit = new DevotedRetainer();
        harness.setGraveyard(player1, List.of(nonSpirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Devoted Retainer"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonSpirit);
    }
}
