package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.m.MossKami;
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

@CardUsed({ScuttlingDeath.class, GrizzlyBears.class, LanternKami.class,
        KamiOfOldStone.class, MossKami.class})
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
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lantern Kami");
        harness.assertNotInGraveyard(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Soulshift may be declined on resolution after choosing a target")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Lantern Kami");
        harness.assertNotInHand(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Soulshift only offers Spirits with mana value 4 or less from your graveyard")
    void soulshiftFiltersByTypeManaValueAndGraveyardOwner() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card cheapSpirit = new LanternKami();
        Card boundarySpirit = new KamiOfOldStone();
        Card nonSpirit = new GrizzlyBears();
        Card expensiveSpirit = new MossKami();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(
                cheapSpirit, boundarySpirit, nonSpirit, expensiveSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                cheapSpirit.getId(), boundarySpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(
                nonSpirit.getId(), expensiveSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift offers no choice with no Spirit in your graveyard")
    void soulshiftNoLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Soulshift requires a target even when its controller intends to decline")
    void soulshiftRequiresTarget() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LanternKami()));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice ability can kill a friendly creature with one toughness")
    void sacAbilityKillsFriendlyCreature() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player1, new LanternKami());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Lantern Kami"));
        harness.assertInGraveyard(player1, "Scuttling Death");
        harness.assertOnBattlefield(player1, "Lantern Kami");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lantern Kami");
        harness.assertInGraveyard(player1, "Lantern Kami");
        harness.assertNotInHand(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Soulshift returns a Spirit at the mana value four boundary before the activated ability resolves")
    void soulshiftReturnsBoundarySpiritFirst() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card spirit = new KamiOfOldStone();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kami of Old Stone");
        harness.assertNotInGraveyard(player1, "Kami of Old Stone");
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Soulshift cannot return a target that left the graveyard before resolution")
    void soulshiftDoesNotReturnRemovedTarget() {
        harness.addToBattlefield(player1, new ScuttlingDeath());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.setGraveyard(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> !card.getId().equals(spirit.getId())).toList());
        harness.setExile(player1, List.of(spirit));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Lantern Kami");
        harness.assertNotInGraveyard(player1, "Lantern Kami");
    }
}
