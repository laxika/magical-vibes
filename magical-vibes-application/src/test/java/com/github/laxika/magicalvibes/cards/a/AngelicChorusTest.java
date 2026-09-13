package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntedWumpus;
import com.github.laxika.magicalvibes.cards.m.Mobilization;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicChorus.class, GiantGrowth.class, GiantSpider.class, GrizzlyBears.class,
        HuntedWumpus.class, Mobilization.class})
class AngelicChorusTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Angelic Chorus puts it on the stack as an enchantment spell")
    void castingAngelicChorusPutsItOnStack() {
        harness.castFromHand(player1, new AngelicChorus(), "{3}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Angelic Chorus resolves onto the battlefield")
    void angelicChorusResolvesOntoBattlefield() {
        harness.castFromHand(player1, new AngelicChorus(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Creature entering triggers Angelic Chorus life gain ability on the stack")
    void creatureEnteringTriggersLifeGainAbility() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Angelic Chorus resolves and increases life total by creature's toughness")
    void angelicChorusLifeGainResolvesCorrectly() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Life gain equals the entering creature's toughness")
    void lifeGainEqualsCreatureToughness() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new GiantSpider(), "{3}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Angelic Chorus does not trigger for opponent's creatures")
    void doesNotTriggerForOpponentCreatures() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two Angelic Choruses trigger separately for the same creature")
    void twoChorusesTriggerSeparately() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Angelic Chorus triggers alongside Hunted Wumpus's enter-the-battlefield ability")
    void triggersAlongsideWumpusEtb() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new HuntedWumpus(), "{3}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Angelic Chorus triggers for a creature token entering")
    void triggersForCreatureToken() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player1, new Mobilization());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Angelic Chorus uses the creature's toughness when its trigger resolves")
    void usesCurrentToughnessWhenTriggerResolves() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent enteringCreature = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, enteringCreature.getId());

        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, enteringCreature)).isEqualTo(5);

        harness.passBothPriorities();
        harness.assertLife(player1, 25);
    }
}
