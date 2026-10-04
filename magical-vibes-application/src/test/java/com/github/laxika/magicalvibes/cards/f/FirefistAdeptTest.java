package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AcademyJourneymage;
import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.c.CabalEvangel;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirefistAdept.class, AcademyJourneymage.class, CabalEvangel.class, BlinkOfAnEye.class})
class FirefistAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger goes on the stack when Firefist Adept enters")
    void etbTriggerGoesOnStack() {
        harness.addToBattlefield(player2, new CabalEvangel());
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Firefist Adept");
    }

    @Test
    @DisplayName("Deals damage equal to Wizards controlled (counts itself)")
    void dealsDamageCountingItself() {
        // Firefist Adept is itself a Wizard, so with no other Wizards it deals 1 damage
        harness.addToBattlefield(player2, new CabalEvangel()); // 2/2
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // 1 damage to 2/2 — Cabal Evangel survives
        harness.assertOnBattlefield(player2, "Cabal Evangel");
    }

    @Test
    @DisplayName("Deals more damage with additional Wizards")
    void dealsMoreDamageWithMultipleWizards() {
        // Firefist Adept + AcademyJourneymage = 2 Wizards, deals 2 damage to 2/2 — lethal
        harness.addToBattlefield(player1, new AcademyJourneymage());
        harness.addToBattlefield(player2, new CabalEvangel()); // 2/2
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Cabal Evangel");
        harness.assertInGraveyard(player2, "Cabal Evangel");
    }

    @Test
    @DisplayName("Counts only your Wizards, not opponent's")
    void countsOnlyControllerWizards() {
        // Opponent has a Wizard, but it shouldn't count for our damage
        harness.addToBattlefield(player2, new AcademyJourneymage()); // opponent's Wizard
        harness.addToBattlefield(player2, new CabalEvangel()); // 2/2 target
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Only 1 Wizard controlled (Firefist Adept itself), so 1 damage to 2/2 — survives
        harness.assertOnBattlefield(player2, "Cabal Evangel");
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new CabalEvangel());
        UUID ownCreatureId = harness.getPermanentId(player1, "Cabal Evangel");
        harness.setHand(player1, List.of(new FirefistAdept()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Firefist Adept enters the battlefield after resolution")
    void firefistAdeptEntersBattlefield() {
        harness.addToBattlefield(player2, new CabalEvangel());
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Firefist Adept");
    }

    @Test
    void countsWizardsAtResolutionAfterAnotherWizardLeaves() {
        harness.addToBattlefield(player1, new AcademyJourneymage());
        harness.addToBattlefield(player2, new CabalEvangel());
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities();

        bouncePermanent(player1, "Academy Journeymage");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2)).singleElement()
                .satisfies(target -> assertThat(target.getMarkedDamage()).isEqualTo(1));
    }

    @Test
    void dealsZeroDamageAfterItsOnlyWizardLeaves() {
        harness.addToBattlefield(player2, new CabalEvangel());
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities();

        bouncePermanent(player1, "Firefist Adept");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Firefist Adept");
        assertThat(gd.playerBattlefields.get(player2)).singleElement()
                .satisfies(target -> assertThat(target.getMarkedDamage()).isZero());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillDealsDamageAfterSourceLeavesWithAnotherWizardRemaining() {
        harness.addToBattlefield(player1, new AcademyJourneymage());
        harness.addToBattlefield(player2, new CabalEvangel());
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities();

        bouncePermanent(player1, "Firefist Adept");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2)).singleElement()
                .satisfies(target -> assertThat(target.getMarkedDamage()).isEqualTo(1));
    }

    @Test
    void triggerDoesNotResolveWhenTargetLeaves() {
        harness.addToBattlefield(player2, new CabalEvangel());
        castFirefistAdept(player2, "Cabal Evangel");
        harness.passBothPriorities();

        bouncePermanent(player2, "Cabal Evangel");
        harness.passBothPriorities();

        harness.assertInHand(player2, "Cabal Evangel");
        harness.assertOnBattlefield(player1, "Firefist Adept");
        assertThat(gd.stack).isEmpty();
    }

    private void bouncePermanent(com.github.laxika.magicalvibes.model.Player owner, String name) {
        UUID targetId = harness.getPermanentId(owner, name);
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void castFirefistAdept(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new FirefistAdept()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
