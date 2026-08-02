package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

class EightAndAHalfTailsTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants protection from white to any permanent you control")
    void permanentYouControlGainsProtectionFromWhite() {
        harness.addToBattlefield(player1, new EightAndAHalfTails());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("The second ability makes a permanent white until end of turn")
    void permanentBecomesWhiteUntilEndOfTurn() {
        harness.addToBattlefield(player1, new EightAndAHalfTails());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("A permanent spell remains white after resolving until end of turn")
    void permanentSpellCarriesTemporaryColor() {
        harness.addToBattlefield(player1, new EightAndAHalfTails());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        UUID spellId = gd.stack.getFirst().getCard().getId();

        harness.activateAbility(player1, 0, 1, null, spellId, Zone.STACK);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Turning a spell white lets protection from white stop it on resolution")
    void whiteSpellCannotTargetPermanentWithProtectionFromWhite() {
        harness.addToBattlefield(player1, new EightAndAHalfTails());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, bearsId);
        harness.passPriority(player2);

        UUID shockId = gd.stack.getFirst().getCard().getId();
        harness.activateAbility(player1, 0, 1, null, shockId, Zone.STACK);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .contains("Grizzly Bears");
    }
}
