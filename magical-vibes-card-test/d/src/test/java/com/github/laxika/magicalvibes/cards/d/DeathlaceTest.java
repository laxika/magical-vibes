package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({Deathlace.class, GrizzlyBears.class, Forest.class, DarkRitual.class, GiantGrowth.class, Unsummon.class})
class DeathlaceTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent becomes black, replacing its previous colors (CR 105.3)")
    void permanentBecomesBlack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deathlace()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("A noncreature permanent can be targeted")
    void noncreaturePermanentBecomesBlack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Deathlace()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("The color change persists indefinitely — it does not wear off at end of turn")
    void colorPersistsPastEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deathlace()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);

        // End-of-turn cleanup expires until-end-of-turn floating effects; Deathlace's is permanent.
        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Targeting a creature spell makes the permanent it becomes black (CR 400.7a)")
    void spellTargetCarriesColorToPermanent() {
        harness.setHand(player1, List.of(new Deathlace(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Grizzly Bears creature spell goes on the stack (index 1; Deathlace stays at index 0).
        harness.castCreature(player1, 1);
        UUID bearsSpellId = gd.stack.getFirst().getCard().getId();

        harness.castAndResolveInstant(player1, 0, bearsSpellId);
        harness.passBothPriorities(); // resolve the Grizzly Bears spell

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).containsExactly(CardColor.BLACK);
    }

    @Test
    void nonpermanentSpellBecomesBlack() {
        harness.setHand(player1, List.of(new Deathlace(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 1);
        Card targetSpell = gd.stack.getFirst().getCard();

        harness.castAndResolveInstant(player1, 0, targetSpell.getId());

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.BLACK);

        harness.passBothPriorities();
    }

    @Test
    void greenInstantBecomesBlackWithoutChangingItsEffect() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deathlace(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 1, target.getId());
        Card targetSpell = gd.stack.getFirst().getCard();
        harness.castAndResolveInstant(player1, 0, targetSpell.getId());

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.BLACK);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.GREEN);
    }

    @Test
    void colorChangeDoesNotFollowCreatureThroughHandAndRecasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Deathlace(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLACK);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInHand(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, findPermanent(player1, "Grizzly Bears")))
                .containsExactly(CardColor.GREEN);
    }
}
