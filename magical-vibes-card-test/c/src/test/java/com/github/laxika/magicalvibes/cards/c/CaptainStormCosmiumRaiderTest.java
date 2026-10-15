package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.p.PlunderingPirate;
import com.github.laxika.magicalvibes.cards.v.VolatileWanderglyph;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainStormCosmiumRaider.class, AccordersShield.class, GrizzlyBears.class})
class CaptainStormCosmiumRaiderTest extends BaseCardTest {

    @Test
    void artifactEntryPutsCounterOnTargetPirateYouControl() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainStormCosmiumRaider());

        harness.setHand(player1, List.of(new AccordersShield()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, captain.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetNonPirateCreatureYouControl() {
        harness.addToBattlefield(player1, new CaptainStormCosmiumRaider());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new AccordersShield()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void nonartifactEntryDoesNotTrigger() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainStormCosmiumRaider());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(VolatileWanderglyph.class)
    void artifactCreatureEntryTriggers() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainStormCosmiumRaider());
        harness.enterBattlefieldAndReturn(player1, new VolatileWanderglyph());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.handlePermanentChosen(player1, captain.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(VolatileWanderglyph.class)
    void opponentsArtifactEntryDoesNotTrigger() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainStormCosmiumRaider());
        harness.enterBattlefieldAndReturn(player2, new VolatileWanderglyph());

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(VolatileWanderglyph.class)
    void cannotTargetOpponentsPirate() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainStormCosmiumRaider());
        Permanent opposingCaptain = harness.addToBattlefieldAndReturn(player2, new CaptainStormCosmiumRaider());
        harness.enterBattlefieldAndReturn(player1, new VolatileWanderglyph());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCaptain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, captain.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCaptain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Bitterblossom.class, VolatileWanderglyph.class})
    void canPutCounterOnNoncreaturePirate() {
        harness.addToBattlefield(player1, new CaptainStormCosmiumRaider());
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, blossom.getId());
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "PIRATE");

        harness.enterBattlefieldAndReturn(player1, new VolatileWanderglyph());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.handlePermanentChosen(player1, blossom.getId());
        harness.passBothPriorities();

        assertThat(blossom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({ArtificialEvolution.class, VolatileWanderglyph.class})
    void targetThatStopsBeingPirateDoesNotReceiveCounter() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainStormCosmiumRaider());
        harness.enterBattlefieldAndReturn(player1, new VolatileWanderglyph());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.handlePermanentChosen(player1, captain.getId());

        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, captain.getId());
        harness.handleListChoice(player1, "PIRATE");
        harness.handleListChoice(player1, "ELF");
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(MycosynthLattice.class)
    void triggersForItsOwnEntryWhenItEntersAsArtifact() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new CaptainStormCosmiumRaider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent captain = findPermanent(player1, "Captain Storm, Cosmium Raider");
        harness.handlePermanentChosen(player1, captain.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed(PlunderingPirate.class)
    void treasureEntryCanPutCounterOnAnotherPirate() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainStormCosmiumRaider());
        Permanent pirate = harness.enterBattlefieldAndReturn(player1, new PlunderingPirate());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pirate.getId());
        harness.passBothPriorities();

        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
