package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.v.VeilOfSecrecy;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InTheWebOfWar.class, GnarledMass.class, Opalescence.class, VeilOfSecrecy.class})
class InTheWebOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control entering gets +2/+0 and haste")
    void boostsAndHastesEnteringCreature() {
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        Permanent mass = findPermanent(player1, "Gnarled Mass");
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(3);
        assertThat(mass.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The boost and haste wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mass = findPermanent(player1, "Gnarled Mass");
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(3);
        assertThat(mass.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Triggers separately for each creature you control that enters")
    void triggersForEachEnteringCreature() {
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        resolveAllTriggers();
        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gnarled Mass")).hasSize(2)
                .allSatisfy(mass -> {
                    assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(5);
                    assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(3);
                    assertThat(mass.hasKeyword(Keyword.HASTE)).isTrue();
                });
    }

    @Test
    @DisplayName("Does not trigger for a creature an opponent controls")
    void noTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent mass = findPermanent(player2, "Gnarled Mass");
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(3);
        assertThat(mass.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies each boost the same entering creature")
    void multipleCopiesStackTheirBoosts() {
        harness.addToBattlefield(player1, new InTheWebOfWar());
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        resolveAllTriggers();

        Permanent mass = findPermanent(player1, "Gnarled Mass");
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(3);
        assertThat(mass.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creatures already on the battlefield do not receive the entering creature's boost")
    void doesNotBoostExistingCreatures() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        harness.addToBattlefield(player1, new InTheWebOfWar());

        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(existing.hasKeyword(Keyword.HASTE)).isFalse();
        Permanent entering = findPermanents(player1, "Gnarled Mass").get(1);
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(5);
        assertThat(entering.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed({InTheWebOfWar.class, Opalescence.class})
    @DisplayName("Triggers for itself when Opalescence makes it enter as a creature")
    void boostsItselfWhenEnteringAsCreature() {
        harness.addToBattlefield(player1, new Opalescence());

        harness.castFromHand(player1, new InTheWebOfWar(), "{3}{R}{R}");
        resolveAllTriggers();

        Permanent web = findPermanent(player1, "In the Web of War");
        assertThat(gqs.getEffectivePower(gd, web)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, web)).isEqualTo(5);
        assertThat(web.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed({InTheWebOfWar.class, GnarledMass.class, VeilOfSecrecy.class})
    @DisplayName("Shroud gained in response does not prevent the nontargeting boost")
    void shroudDoesNotPreventBoost() {
        harness.addToBattlefield(player1, new InTheWebOfWar());
        harness.castFromHand(player1, new GnarledMass(), "{1}{G}{G}");
        harness.passBothPriorities();
        Permanent mass = findPermanent(player1, "Gnarled Mass");

        harness.setHand(player1, List.of(new VeilOfSecrecy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, mass.getId());
        resolveAllTriggers();

        assertThat(mass.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mass)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mass)).isEqualTo(3);
        assertThat(mass.hasKeyword(Keyword.HASTE)).isTrue();
    }
}
