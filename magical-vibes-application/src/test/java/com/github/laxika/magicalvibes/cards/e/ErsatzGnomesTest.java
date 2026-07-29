package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErsatzGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: target creature spell becomes colorless, and stays colorless as a permanent (CR 400.7a)")
    void spellBecomesColorless() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        UUID bearsSpellId = gd.stack.getFirst().getCard().getId();

        harness.activateAbility(player1, 0, 0, null, bearsSpellId, Zone.STACK);
        harness.passBothPriorities(); // resolve the ability
        harness.passBothPriorities(); // resolve the Grizzly Bears spell

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("{T}: target permanent becomes colorless until end of turn")
    void permanentBecomesColorless() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("The permanent's colorless setting wears off at end of turn")
    void permanentColorlessWearsOff() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("The spell ability cannot be pointed at a permanent on the battlefield")
    void spellAbilityRejectsPermanentTarget() {
        addCreatureReady(player1, new ErsatzGnomes());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
