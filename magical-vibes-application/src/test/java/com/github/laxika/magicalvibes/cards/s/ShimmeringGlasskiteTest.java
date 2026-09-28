package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.m.MatsuTribeSniper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShimmeringGlasskite.class, FirstVolley.class, MatsuTribeSniper.class})
class ShimmeringGlasskiteTest extends BaseCardTest {

    private UUID addGlasskite() {
        return addCreatureReady(player1, new ShimmeringGlasskite()).getId();
    }

    @Test
    @DisplayName("Counters the first spell that targets it each turn")
    void countersFirstSpellEachTurn() {
        UUID kiteId = addGlasskite();

        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, kiteId);

        // First Volley plus the counter trigger on top.
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        harness.assertOnBattlefield(player1, "Shimmering Glasskite");
        harness.assertInGraveyard(player2, "First Volley");
    }

    @Test
    @DisplayName("Counters an activated ability that targets it")
    void countersTargetingAbility() {
        UUID kiteId = addGlasskite();

        Permanent sniper = addCreatureReady(player2, new MatsuTribeSniper());

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sniper), null, kiteId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        Permanent kite = findPermanent(player1, "Shimmering Glasskite");
        assertThat(kite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counters a spell cast by its own controller")
    void countersControllersOwnSpell() {
        UUID kiteId = addGlasskite();

        harness.setHand(player1, List.of(new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, kiteId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shimmering Glasskite");
        harness.assertInGraveyard(player1, "First Volley");
    }

    @Test
    @DisplayName("A second spell the same turn is not countered")
    void secondSpellSameTurnNotCountered() {
        UUID kiteId = addGlasskite();

        harness.setHand(player2, List.of(new FirstVolley(), new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, kiteId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Shimmering Glasskite");

        // Second spell the same turn: the trigger does not fire again.
        harness.castInstant(player2, 0, kiteId);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent kite = findPermanent(player1, "Shimmering Glasskite");
        assertThat(kite.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The first-target limit is shared by abilities and spells")
    void spellAfterAbilitySameTurnIsNotCountered() {
        UUID kiteId = addGlasskite();
        Permanent sniper = addCreatureReady(player2, new MatsuTribeSniper());

        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sniper), null, kiteId);
        harness.passBothPriorities();

        harness.castInstant(player2, 0, kiteId);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent kite = findPermanent(player1, "Shimmering Glasskite");
        assertThat(kite.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The first-target trigger resets at the start of a new turn")
    void firstTargetTriggerResetsNextTurn() {
        UUID kiteId = addGlasskite();

        harness.setHand(player2, List.of(new FirstVolley(), new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, kiteId);
        harness.passBothPriorities();

        declareAttackers(List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, kiteId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "First Volley");
        assertThat(findPermanent(player1, "Shimmering Glasskite").getMarkedDamage()).isZero();
    }
}
