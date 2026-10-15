package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AwakeningOfVituGhazi;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FreewindFalcon;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.cards.t.TeferisRealm;
import com.github.laxika.magicalvibes.cards.u.UndiscoveredParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        EyeOfSingularity.class,
        AwakeningOfVituGhazi.class,
        Boomerang.class,
        FreewindFalcon.class,
        Plains.class,
        RiverBoa.class,
        TeferisRealm.class,
        UndiscoveredParadise.class
})
class EyeOfSingularityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys duplicate-named non-basic permanents and spares unique ones")
    void etbDestroysDuplicates() {
        harness.addToBattlefield(player1, new RiverBoa());
        harness.addToBattlefield(player2, new RiverBoa());
        harness.addToBattlefield(player1, new FreewindFalcon());

        harness.castFromHand(player1, new EyeOfSingularity(), "{3}{W}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "River Boa");
        harness.assertNotOnBattlefield(player2, "River Boa");
        harness.assertOnBattlefield(player1, "Freewind Falcon");
        harness.assertOnBattlefield(player1, "Eye of Singularity");
    }

    @Test
    @DisplayName("ETB does not destroy basic lands that share a name")
    void etbSparesBasicLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());

        harness.castFromHand(player1, new EyeOfSingularity(), "{3}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        assertThat(findPermanents(player1, "Plains")).hasSize(2);
        assertThat(findPermanents(player2, "Plains")).hasSize(1);
    }

    @Test
    @DisplayName("ETB destruction can't be regenerated")
    void etbDestructionCannotBeRegenerated() {
        Permanent shielded = addCreatureReady(player1, new RiverBoa());
        shielded.setRegenerationShield(1);
        harness.addToBattlefield(player2, new RiverBoa());

        harness.castFromHand(player1, new EyeOfSingularity(), "{3}{W}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "River Boa");
        harness.assertNotOnBattlefield(player2, "River Boa");
        harness.assertInGraveyard(player1, "River Boa");
        harness.assertInGraveyard(player2, "River Boa");
    }

    @Test
    @DisplayName("Non-basic permanent entering destroys all other permanents with that name")
    void enteringDestroysOthersWithSameName() {
        harness.addToBattlefield(player1, new EyeOfSingularity());
        Permanent shielded = addCreatureReady(player2, new RiverBoa());
        shielded.setRegenerationShield(1);
        harness.addToBattlefield(player2, new RiverBoa());

        harness.castFromHand(player1, new RiverBoa(), "{1}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "River Boa");
        assertThat(findPermanents(player2, "River Boa")).isEmpty();
        harness.assertInGraveyard(player2, "River Boa");
    }

    @Test
    @DisplayName("Non-basic land entering destroys other permanents with that name")
    void nonBasicLandEnteringDestroysOthersWithSameName() {
        harness.addToBattlefield(player1, new EyeOfSingularity());
        harness.addToBattlefield(player2, new UndiscoveredParadise());

        harness.setHand(player1, List.of(new UndiscoveredParadise()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Undiscovered Paradise");
        harness.assertNotOnBattlefield(player2, "Undiscovered Paradise");
        harness.assertInGraveyard(player2, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("The world rule keeps the newest world permanent")
    void worldRuleKeepsNewestWorldPermanent() {
        harness.addToBattlefield(player1, new TeferisRealm());

        harness.castFromHand(player1, new EyeOfSingularity(), "{3}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Eye of Singularity");
        harness.assertNotOnBattlefield(player1, "Teferi's Realm");
        harness.assertInGraveyard(player1, "Teferi's Realm");
    }

    @Test
    @DisplayName("Basic land entering does not trigger destroy-others")
    void basicLandEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new EyeOfSingularity());
        harness.addToBattlefield(player2, new Plains());

        harness.setHand(player1, List.of(new Plains()));
        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Plains");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering permanent returned to hand still destroys other permanents with its last known name")
    void enteringPermanentBouncedBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new EyeOfSingularity());
        harness.addToBattlefield(player2, new RiverBoa());
        harness.castFromHand(player1, new RiverBoa(), "{1}{G}");
        harness.passBothPriorities();

        Permanent entering = findPermanent(player1, "River Boa");
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, entering.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "River Boa");

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "River Boa");
        harness.assertInGraveyard(player2, "River Boa");
        harness.assertInHand(player1, "River Boa");
    }

    @Test
    @DisplayName("A permanent with a changed name is spared when its printed namesake enters")
    void enteringPermanentDoesNotDestroyRenamedPermanent() {
        harness.addToBattlefield(player1, new EyeOfSingularity());
        Permanent renamed = harness.addToBattlefieldAndReturn(player1, new UndiscoveredParadise());
        harness.setHand(player1, List.of(new AwakeningOfVituGhazi()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, renamed.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new UndiscoveredParadise()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(renamed);
        assertThat(findPermanents(player1, "Undiscovered Paradise")).hasSize(2);
        harness.assertNotInGraveyard(player1, "Undiscovered Paradise");
    }

    @Test
    @DisplayName("Entering a unique permanent still triggers but leaves existing permanents alone")
    void uniquePermanentEnteringLeavesOthersAlone() {
        harness.addToBattlefield(player1, new EyeOfSingularity());
        harness.addToBattlefield(player2, new RiverBoa());
        harness.castFromHand(player1, new FreewindFalcon(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Freewind Falcon");
        harness.assertOnBattlefield(player2, "River Boa");
    }
}
