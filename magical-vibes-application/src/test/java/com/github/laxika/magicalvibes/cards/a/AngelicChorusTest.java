package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HuntedWumpus;
import com.github.laxika.magicalvibes.cards.m.Mobilization;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Terror;
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
        HuntedWumpus.class, Mobilization.class, Shock.class, GloriousAnthem.class,
        Naturalize.class, Terror.class})
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

        Permanent enteringCreature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, enteringCreature.getId());
        assertThat(gqs.getEffectiveToughness(gd, enteringCreature)).isEqualTo(5);

        harness.passBothPriorities();
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Angelic Chorus uses last known toughness if the creature leaves before resolution")
    void usesLastKnownToughnessWhenCreatureLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent enteringCreature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, enteringCreature.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Noncreature permanents do not trigger Angelic Chorus")
    void doesNotTriggerForNoncreaturePermanent() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new Mobilization(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Angelic Chorus includes continuous toughness bonuses")
    void includesContinuousToughnessBonus() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Last known toughness includes continuous bonuses before the creature died")
    void lastKnownToughnessIncludesContinuousBonus() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent enteringCreature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, enteringCreature.getId());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Removing Angelic Chorus does not remove its pending life gain")
    void resolvesAfterChorusLeavesBattlefield() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent chorus = findPermanent(player1, "Angelic Chorus");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, chorus.getId());
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Each successive creature entry gains life independently")
    void gainsLifeForSuccessiveCreatureEntries() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();
        harness.castFromHand(player1, new GiantSpider(), "{3}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 26);
    }
}
