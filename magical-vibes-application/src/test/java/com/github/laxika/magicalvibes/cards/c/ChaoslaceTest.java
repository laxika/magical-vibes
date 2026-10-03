package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({Chaoslace.class, GrizzlyBears.class, Forest.class, DarkRitual.class, Unsummon.class})
class ChaoslaceTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent becomes red, replacing its previous colors (CR 105.3)")
    void permanentBecomesRed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Chaoslace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("A noncreature permanent can be targeted")
    void noncreaturePermanentBecomesRed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Chaoslace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The color change has no duration — it does not wear off at end of turn")
    void colorPersistsPastEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Chaoslace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);

        // End-of-turn cleanup expires until-end-of-turn floating effects; Chaoslace's is permanent.
        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Targeting a creature spell makes the permanent it becomes red (CR 400.7a)")
    void spellTargetCarriesColorToPermanent() {
        harness.setHand(player1, List.of(new Chaoslace(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Grizzly Bears creature spell goes on the stack (index 1; Chaoslace stays at index 0).
        harness.castCreature(player1, 1);
        UUID bearsSpellId = gd.stack.getFirst().getCard().getId();

        harness.castAndResolveInstant(player1, 0, bearsSpellId);
        harness.passBothPriorities(); // resolve the Grizzly Bears spell

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("A nonpermanent spell becomes red while on the stack but not after it leaves")
    void nonpermanentSpellColorDoesNotPersistAfterLeavingStack() {
        DarkRitual targetSpell = new DarkRitual();
        harness.setHand(player1, List.of(new Chaoslace(), targetSpell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 1);
        UUID targetSpellId = gd.stack.getFirst().getCard().getId();

        harness.castAndResolveInstant(player1, 0, targetSpellId);

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.RED);

        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.BLACK);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's spell becomes red while on the stack")
    void opponentSpellBecomesRed() {
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player2, List.of(ritual));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0);
        harness.setHand(player1, List.of(new Chaoslace()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ritual.getId());

        assertThat(gqs.getEffectiveCardColors(gd, ritual)).containsExactly(CardColor.RED);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Dark Ritual");
        assertThat(gqs.getEffectiveCardColors(gd, ritual)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("A returned creature loses the color change when cast again")
    void returnedCreatureDoesNotKeepColorChange() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Chaoslace(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.RED);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInHand(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, findPermanent(player1, "Grizzly Bears")))
                .containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Chaoslace does not change a creature that leaves before resolution")
    void removedTargetIsNotChanged() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Chaoslace()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Chaoslace");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveCardColors(gd, bears.getCard())).containsExactly(CardColor.GREEN);
    }
}
