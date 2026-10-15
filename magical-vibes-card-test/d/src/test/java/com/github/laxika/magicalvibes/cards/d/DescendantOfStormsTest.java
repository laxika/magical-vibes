package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DescendantOfStorms.class, RayOfCommand.class, Unsummon.class})
class DescendantOfStormsTest extends BaseCardTest {

    private static final String COUNTERS = "Put 1 +1/+1 counter on this permanent";
    private static final String SPIRIT = "Create a 1/1 white Spirit creature token";

    @Test
    void payingManaCanPutACounterOnDescendant() {
        Permanent descendant = addCreatureReady(player1, new DescendantOfStorms());
        addManaForEndure();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, COUNTERS);

        assertThat(descendant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void payingManaCanCreateASpirit() {
        addCreatureReady(player1, new DescendantOfStorms());
        addManaForEndure();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, SPIRIT);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    void decliningPaymentDoesNothing() {
        Permanent descendant = addCreatureReady(player1, new DescendantOfStorms());
        addManaForEndure();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(descendant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void currentControllerChoosesEndureAndCreatesTheSpiritAfterControlChanges() {
        Permanent descendant = addCreatureReady(player1, new DescendantOfStorms());
        addManaForEndure();
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player2, 0, descendant.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(descendant);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player2, SPIRIT);

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
        assertThat(descendant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void payingAfterDescendantLeavesTheBattlefieldCreatesASpirit() {
        Permanent descendant = addCreatureReady(player1, new DescendantOfStorms());
        addManaForEndure();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player2, 0, descendant.getId());
        harness.assertInHand(player1, "Descendant of Storms");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void addManaForEndure() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
