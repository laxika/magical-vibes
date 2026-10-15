package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.r.Redirect;
import com.github.laxika.magicalvibes.cards.c.CallToMind;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThousandYearStorm.class, Divination.class, RuneclawBear.class, LightningBolt.class,
        Redirect.class, CallToMind.class, Cancel.class})
class ThousandYearStormTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an instant or sorcery once for each prior matching spell cast by its controller")
    void copiesForEachPriorInstantOrSorcery() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        gd.recordSpellCast(player1.getId(), new LightningBolt());
        gd.recordSpellCast(player1.getId(), new Divination());
        gd.recordSpellCast(player1.getId(), new RuneclawBear());
        gd.recordSpellCast(player2.getId(), new LightningBolt());

        harness.castFromHand(player1, new Divination(), "{2}{U}");

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy))
                .allMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("Does not count creature spells or spells cast by another player")
    void ignoresOtherSpellTypesAndPlayers() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        gd.recordSpellCast(player1.getId(), new RuneclawBear());
        gd.recordSpellCast(player2.getId(), new LightningBolt());

        harness.castFromHand(player1, new Divination(), "{2}{U}");

        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Thousand-Year Storm"));
    }

    @Test
    void countsEarlierCastOfTheSameReturnedCard() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.setHand(player1, List.of(new CallToMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, bolt.getId());
        harness.assertInHand(player1, "Lightning Bolt");
        harness.addToBattlefield(player1, new ThousandYearStorm());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    void copiesTargetsAsTheyExistWhenTheTriggerResolves() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        gd.recordSpellCast(player1.getId(), new Divination());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    void mayRetargetCopyWithoutChangingOriginal() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        gd.recordSpellCast(player1.getId(), new Divination());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.getSpellsCastThisTurn(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void untargetedCopiesResolveAndDoNotCountAsCasts() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        gd.recordSpellCast(player1.getId(), new LightningBolt());
        harness.setLibrary(player1, List.of(new Divination(), new Divination(),
                new Divination(), new Divination(), new Divination()));
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getSpellsCastThisTurn(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillCopiesOriginalAfterItIsCountered() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        gd.recordSpellCast(player1.getId(), new Divination());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsInstantDoesNotTriggerStorm() {
        harness.addToBattlefield(player1, new ThousandYearStorm());
        gd.recordSpellCast(player2.getId(), new Divination());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
        assertThat(gd.stack).isEmpty();
    }
}
