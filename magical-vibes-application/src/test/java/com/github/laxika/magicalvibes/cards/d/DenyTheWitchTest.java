package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DenyTheWitch.class, FountainOfYouth.class, GrizzlyBears.class, AngelOfMercy.class})
class DenyTheWitchTest extends BaseCardTest {

    @Test
    void countersSpellAndItsControllerLosesLifeEqualToYourCreatureCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new DenyTheWitch()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Deny the Witch");
    }

    @Test
    void countersActivatedAbility() {
        FountainOfYouth fountain = new FountainOfYouth();
        harness.addToBattlefield(player2, fountain);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new DenyTheWitch()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, fountain.getId());

        harness.assertLife(player2, 19);
        org.assertj.core.api.Assertions.assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void countersTriggeredAbilityWithoutRemovingItsSource() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DenyTheWitch()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        AngelOfMercy angel = new AngelOfMercy();
        harness.enterBattlefieldAndReturn(player2, angel);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Angel of Mercy");
        harness.assertNotInGraveyard(player2, "Angel of Mercy");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    @Test
    void countersSpellWithNoCreaturesWithoutLifeLoss() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new DenyTheWitch()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCauseLifeLossWhenTargetHasAlreadyLeftTheStack() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new DenyTheWitch(), new DenyTheWitch()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertLife(player2, 19);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCounterYourOwnSpellAndYouLoseLife() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target, new DenyTheWitch()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.castCreature(player1, 0);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }
}
