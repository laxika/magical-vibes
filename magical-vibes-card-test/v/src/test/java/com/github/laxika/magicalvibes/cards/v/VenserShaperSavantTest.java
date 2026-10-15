package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Foresee;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenserShaperSavant.class, HorizonCanopy.class, Foresee.class})
class VenserShaperSavantTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentToItsOwnersHand() {
        HorizonCanopy targetCard = new HorizonCanopy();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.castFromHand(player1, new VenserShaperSavant(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Horizon Canopy");
        harness.assertNotInHand(player2, "Horizon Canopy");
        harness.assertNotOnBattlefield(player2, "Horizon Canopy");
    }

    @Test
    void returnsTargetSpellToItsOwnersHand() {
        Foresee targetSpell = new Foresee();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, targetSpell, "{3}{U}");
        UUID targetSpellId = targetSpell.getId();
        harness.castFromHand(player1, new VenserShaperSavant(), "{2}{U}{U}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetSpellId);
        harness.handlePermanentChosen(player1, targetSpellId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Venser, Shaper Savant");
        harness.assertInHand(player2, "Foresee");
        harness.assertNotInGraveyard(player2, "Foresee");
    }

    @Test
    void mustReturnItselfWhenItIsTheOnlyPermanent() {
        harness.castFromHand(player1, new VenserShaperSavant(), "{2}{U}{U}");
        harness.passBothPriorities();

        UUID venserId = harness.getPermanentId(player1, "Venser, Shaper Savant");
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(venserId);
        harness.handlePermanentChosen(player1, venserId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Venser, Shaper Savant");
        harness.assertNotOnBattlefield(player1, "Venser, Shaper Savant");
    }

    @Test
    void cannotTargetAnActivatedAbilityOnTheStack() {
        harness.addToBattlefield(player2, new HorizonCanopy());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, 1, null, null);
        UUID abilityId = harness.getGameData().stack.getLast().getTargetableId();

        harness.castFromHand(player1, new VenserShaperSavant(), "{2}{U}{U}");
        harness.passBothPriorities();

        UUID venserId = harness.getPermanentId(player1, "Venser, Shaper Savant");
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(venserId)
                .doesNotContain(abilityId);
        harness.handlePermanentChosen(player1, venserId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Venser, Shaper Savant");
        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getTargetableId()).isEqualTo(abilityId);
    }
}
