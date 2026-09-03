package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErsatzGnomes.class, FemerefScouts.class, DarkRitual.class})
class ErsatzGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: target creature spell becomes colorless, and stays colorless as a permanent (CR 400.7a)")
    void spellBecomesColorless() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.setHand(player1, List.of(new FemerefScouts()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        UUID scoutsSpellId = gd.stack.getFirst().getCard().getId();

        harness.activateAbility(player1, 0, 0, null, scoutsSpellId, Zone.STACK);
        harness.passBothPriorities(); // resolve the ability
        harness.passBothPriorities(); // resolve the Femeref Scouts spell

        Permanent scouts = findPermanent(player1, "Femeref Scouts");
        assertThat(gqs.getEffectiveColors(gd, scouts)).isEmpty();
    }

    @Test
    @DisplayName("Targeting a nonpermanent spell does not change its color after it leaves the stack")
    void nonpermanentSpellColorDoesNotCarryToGraveyard() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        UUID ritualSpellId = gd.stack.getFirst().getCard().getId();

        harness.activateAbility(player1, 0, 0, null, ritualSpellId, Zone.STACK);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardColors(gd, gd.stack.getFirst().getCard())).isEmpty();

        harness.passBothPriorities();

        Card ritual = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(DarkRitual.class::isInstance)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectiveCardColors(gd, ritual)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("{T}: target permanent becomes colorless until end of turn")
    void permanentBecomesColorless() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.addToBattlefield(player2, new FemerefScouts());
        Permanent scouts = findPermanent(player2, "Femeref Scouts");

        assertThat(gqs.getEffectiveColors(gd, scouts)).containsExactly(CardColor.WHITE);

        harness.activateAbility(player1, 0, 1, null, scouts.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, scouts)).isEmpty();
    }

    @Test
    @DisplayName("The permanent's colorless setting wears off at end of turn")
    void permanentColorlessWearsOff() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.addToBattlefield(player2, new FemerefScouts());
        Permanent scouts = findPermanent(player2, "Femeref Scouts");

        harness.activateAbility(player1, 0, 1, null, scouts.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, scouts)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, scouts)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("The spell ability cannot be pointed at a permanent on the battlefield")
    void spellAbilityRejectsPermanentTarget() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.addToBattlefield(player2, new FemerefScouts());
        Permanent scouts = findPermanent(player2, "Femeref Scouts");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, scouts.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
