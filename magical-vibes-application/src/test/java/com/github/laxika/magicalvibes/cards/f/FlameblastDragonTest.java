package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlameblastDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {X}{R} deals X damage to a chosen creature and kills it")
    void payingDealsXDamageToCreature() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3); // enough for {2}{R}

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve trigger -> prompts for X
        harness.handleXValueChosen(player1, 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paying {X}{R} deals X damage to a chosen player")
    void payingDealsXDamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FlameblastDragon());
        addCreatureReady(player2, new GrizzlyBears()); // possible blocker halts combat before damage
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        // Only the trigger damage has been dealt (combat damage awaits block declaration).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Choosing X=0 declines: no damage is dealt")
    void decliningDealsNoDamage() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without enough mana for {X}{R} the ability does nothing")
    void cannotPayDoesNothing() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1); // only pays {R}, so max X is 0

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // trigger resolves, but no X can be paid

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attack trigger cannot target a land — any target is creature/planeswalker/player")
    void cannotTargetLand() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(bears.getId(), player1.getId(), player2.getId())
                .doesNotContain(mountain.getId());
    }

    /**
     * The attack trigger's any-target narrowing is evaluated from the declared target rather than
     * re-implemented, so it reads the planeswalker type after layer 4 (CR 613.1d). A planeswalker
     * Imprisoned in the Moon turned into a colorless land is no longer an any target (CR 115.4) —
     * the same answer the spell path gives Lightning Bolt.
     */
    @Test
    @DisplayName("Attack trigger offers a planeswalker, but not one Imprisoned in the Moon turned into a land")
    void offersPlaneswalkerUnlessLayerFourTookTheTypeAway() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        Permanent aura = new Permanent(new ImprisonedInTheMoon());
        aura.setAttachedTo(jace.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(chandra.getId())
                .doesNotContain(jace.getId());
    }

    @Test
    @DisplayName("Empty mana pool with untapped Mountains still prompts to pay {X}{R}")
    void emptyPoolWithUntappedLandsStillPromptsForX() {
        addCreatureReady(player1, new FlameblastDragon());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isGreaterThanOrEqualTo(2);
        assertThat(choice.prompt()).containsIgnoringCase("you may pay");
    }
}
