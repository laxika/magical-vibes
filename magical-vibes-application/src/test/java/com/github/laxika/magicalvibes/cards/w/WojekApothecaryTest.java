package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BenevolentAncestor;
import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.g.GlassGolem;
import com.github.laxika.magicalvibes.cards.g.GolgariBrownscale;
import com.github.laxika.magicalvibes.cards.r.RainOfEmbers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WojekApothecary.class, BenevolentAncestor.class, BorosRecruit.class, GolgariBrownscale.class,
        GlassGolem.class, RainOfEmbers.class})
class WojekApothecaryTest extends BaseCardTest {

    @Test
    @DisplayName("Shields the target and every creature sharing a color with it")
    void shieldsTargetAndColorSharingCreatures() {
        Permanent apothecary = addCreatureReady(player1, new WojekApothecary());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BenevolentAncestor());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());

        activate(apothecary, target);

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(matchingCreature.getDamagePreventionShield()).isEqualTo(1);
        assertThat(differentColorCreature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevents the next damage to each affected creature")
    void preventsNextDamageToEachAffectedCreature() {
        Permanent apothecary = addCreatureReady(player1, new WojekApothecary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        Permanent differentColorCreature = harness.addToBattlefieldAndReturn(player2, new GolgariBrownscale());

        activate(apothecary, target);

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(matchingCreature.getMarkedDamage()).isZero();
        assertThat(differentColorCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A colorless target does not shield other colorless creatures")
    void colorlessTargetOnlyAffectsItself() {
        Permanent apothecary = addCreatureReady(player1, new WojekApothecary());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GlassGolem());
        Permanent otherColorlessCreature = harness.addToBattlefieldAndReturn(player2, new GlassGolem());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());

        activate(apothecary, target);

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        assertThat(otherColorlessCreature.getDamagePreventionShield()).isZero();
        assertThat(coloredCreature.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Does not shield a creature that enters after the ability resolves")
    void creaturesEnteringAfterResolutionAreNotAffected() {
        Permanent apothecary = addCreatureReady(player1, new WojekApothecary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        activate(apothecary, target);

        Permanent laterMatchingCreature = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());
        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(matchingCreature.getMarkedDamage()).isZero();
        assertThat(laterMatchingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each shield prevents only one damage across successive damage events")
    void shieldsAreConsumedByTheFirstDamageEvent() {
        Permanent apothecary = addCreatureReady(player1, new WojekApothecary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player1, new BenevolentAncestor());

        activate(apothecary, target);

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(matchingCreature.getMarkedDamage()).isZero();

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(matchingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Wojek Apothecary");
    }

    @Test
    @DisplayName("Shields matching creatures that enter before resolution")
    void creaturesEnteringBeforeResolutionAreAffected() {
        Permanent apothecary = addCreatureReady(player1, new WojekApothecary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(apothecary),
                null, target.getId());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        harness.passBothPriorities();

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(matchingCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Boros Recruit");
    }

    @Test
    @DisplayName("Can target itself and prevent damage to itself and other white creatures")
    void canTargetItself() {
        Permanent apothecary = addCreatureReady(player1, new WojekApothecary());
        Permanent matchingCreature = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor());

        activate(apothecary, apothecary);

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();
        assertThat(apothecary.getMarkedDamage()).isZero();
        assertThat(matchingCreature.getMarkedDamage()).isZero();
        assertThat(apothecary.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Wojek Apothecary");
    }

    private void activate(Permanent apothecary, Permanent target) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(apothecary),
                null, target.getId());
        harness.passBothPriorities();
    }
}
