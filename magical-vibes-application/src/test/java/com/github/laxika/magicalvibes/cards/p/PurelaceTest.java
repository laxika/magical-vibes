package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Purelace.class, GrizzlyBears.class, Forest.class, DarkRitual.class})
class PurelaceTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent becomes white, replacing its previous colors (CR 105.3)")
    void permanentBecomesWhite() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Purelace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("A noncreature permanent can be targeted")
    void noncreaturePermanentBecomesWhite() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Purelace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("The color change has no duration — it does not wear off at end of turn")
    void colorPersistsPastEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Purelace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);

        // End-of-turn cleanup expires until-end-of-turn floating effects; Purelace's is permanent.
        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Targeting a creature spell makes the permanent it becomes white (CR 400.7a)")
    void spellTargetCarriesColorToPermanent() {
        harness.setHand(player1, List.of(new Purelace(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Grizzly Bears creature spell goes on the stack (index 1; Purelace stays at index 0).
        harness.castCreature(player1, 1);
        UUID bearsSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, bearsSpellId);
        harness.passBothPriorities(); // resolve Purelace on the spell
        harness.passBothPriorities(); // resolve the Grizzly Bears spell

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("A nonpermanent spell on the stack becomes white")
    void nonpermanentSpellBecomesWhite() {
        harness.setHand(player1, List.of(new Purelace(), new DarkRitual()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 1);
        Card targetSpell = gd.stack.getFirst().getCard();
        harness.castInstant(player1, 0, targetSpell.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.WHITE);

        harness.passBothPriorities();
    }
}
