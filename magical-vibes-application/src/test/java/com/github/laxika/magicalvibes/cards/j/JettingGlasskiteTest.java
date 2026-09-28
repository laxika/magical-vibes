package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed({JettingGlasskite.class, FirstVolley.class, MatsuTribeSniper.class})
class JettingGlasskiteTest extends BaseCardTest {

    private UUID addGlasskite() {
        return addCreatureReady(player1, new JettingGlasskite()).getId();
    }

    @Test
    @DisplayName("Counters the first spell that targets it each turn")
    void countersFirstSpellEachTurn() {
        UUID glasskiteId = addGlasskite();

        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, glasskiteId);

        // First Volley plus Jetting Glasskite's counter trigger on top.
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        harness.assertOnBattlefield(player1, "Jetting Glasskite");
        harness.assertInGraveyard(player2, "First Volley");

        Permanent glasskite = findPermanent(player1, "Jetting Glasskite");
        assertThat(glasskite.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Counters an activated ability that targets it")
    void countersTargetingAbility() {
        UUID glasskiteId = addGlasskite();

        harness.addToBattlefield(player2, new MatsuTribeSniper());
        Permanent sniper = findPermanent(player2, "Matsu-Tribe Sniper");
        sniper.setSummoningSick(false);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sniper), null, glasskiteId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        Permanent glasskite = findPermanent(player1, "Jetting Glasskite");
        assertThat(glasskite.isTapped()).isFalse();
        assertThat(glasskite.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Counters its own controller's spell (no controller restriction)")
    void countersControllersOwnSpell() {
        UUID glasskiteId = addGlasskite();

        harness.setHand(player1, List.of(new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, glasskiteId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        harness.assertOnBattlefield(player1, "Jetting Glasskite");
        harness.assertInGraveyard(player1, "First Volley");
    }

    @Test
    @DisplayName("A second spell the same turn is not countered")
    void secondSpellSameTurnNotCountered() {
        UUID glasskiteId = addGlasskite();

        harness.setHand(player2, List.of(new FirstVolley(), new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 4);

        // First volley is countered.
        harness.castInstant(player2, 0, glasskiteId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Jetting Glasskite");

        // Second volley the same turn: the trigger does not fire again; only the volley is on the stack.
        harness.castInstant(player2, 0, glasskiteId);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("First Volley");

        harness.passBothPriorities(); // resolve the volley; 1 damage on a 4/4

        Permanent glasskite = findPermanent(player1, "Jetting Glasskite");
        assertThat(glasskite.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The once-per-turn limit is shared by spells and abilities")
    void spellAfterAbilitySameTurnIsNotCountered() {
        UUID glasskiteId = addGlasskite();

        harness.addToBattlefield(player2, new MatsuTribeSniper());
        Permanent sniper = findPermanent(player2, "Matsu-Tribe Sniper");
        sniper.setSummoningSick(false);
        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(sniper), null, glasskiteId);
        harness.passBothPriorities();

        harness.castInstant(player2, 0, glasskiteId);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent glasskite = findPermanent(player1, "Jetting Glasskite");
        assertThat(glasskite.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The first-target trigger resets at the start of a new turn")
    void firstTargetTriggerResetsNextTurn() {
        UUID glasskiteId = addGlasskite();

        harness.setHand(player2, List.of(new FirstVolley(), new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player2, 0, glasskiteId);
        harness.passBothPriorities();

        declareAttackers(List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, glasskiteId);
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "First Volley");
        assertThat(findPermanent(player1, "Jetting Glasskite").getMarkedDamage()).isZero();
    }
}
