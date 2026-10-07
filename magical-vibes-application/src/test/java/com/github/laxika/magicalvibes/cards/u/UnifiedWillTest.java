package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({UnifiedWill.class, GrizzlyBears.class, NestInvader.class, Regress.class})
class UnifiedWillTest extends BaseCardTest {

    @Test
    void countersSpellWhenYouControlMoreCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears targetSpell = new GrizzlyBears();

        harness.setHand(player2, List.of(new UnifiedWill()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, targetSpell, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotCounterSpellWhenYouDoNotControlMoreCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears targetSpell = new GrizzlyBears();

        harness.setHand(player2, List.of(new UnifiedWill()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, targetSpell, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Unified Will");
    }

    @Test
    void doesNotCounterWhenNeitherPlayerControlsCreatures() {
        NestInvader targetSpell = new NestInvader();
        harness.setHand(player2, List.of(new UnifiedWill()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castFromHand(player1, targetSpell, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nest Invader");
        harness.assertNotInGraveyard(player1, "Nest Invader");
        harness.assertInGraveyard(player2, "Unified Will");
    }

    @Test
    void losesCreatureAdvantageBeforeResolution() {
        checkCreatureCountAfterResponse(true);
    }

    @Test
    void gainsCreatureAdvantageBeforeResolution() {
        checkCreatureCountAfterResponse(false);
    }

    @Test
    void countersNoncreatureSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Regress targetSpell = new Regress();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new UnifiedWill()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Regress");
        harness.assertOnBattlefield(player2, "Nest Invader");
        harness.assertInGraveyard(player2, "Unified Will");
    }

    @Test
    void canTargetOwnSpellButCannotHaveMoreCreaturesThanItsController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        Regress targetSpell = new Regress();
        harness.setHand(player1, List.of(targetSpell, new UnifiedWill()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, targetSpell.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nest Invader");
        harness.assertNotInGraveyard(player1, "Regress");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nest Invader");
        harness.assertNotOnBattlefield(player1, "Nest Invader");
        harness.assertInGraveyard(player1, "Unified Will");
        harness.assertInGraveyard(player1, "Regress");
    }

    private void checkCreatureCountAfterResponse(boolean losesAdvantage) {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Permanent bouncedCreature = losesAdvantage ? ownCreature
                : harness.addToBattlefieldAndReturn(player1, new NestInvader());
        NestInvader targetSpell = new NestInvader();
        harness.setHand(player2, List.of(new UnifiedWill()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castFromHand(player1, targetSpell, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, bouncedCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (losesAdvantage) {
            harness.passBothPriorities();
            harness.assertOnBattlefield(player1, "Nest Invader");
            harness.assertNotInGraveyard(player1, "Nest Invader");
        } else {
            harness.assertInGraveyard(player1, "Nest Invader");
            harness.assertNotOnBattlefield(player1, "Nest Invader");
        }
        harness.assertInHand(losesAdvantage ? player2 : player1, "Nest Invader");
        harness.assertInGraveyard(player2, "Unified Will");
    }
}
