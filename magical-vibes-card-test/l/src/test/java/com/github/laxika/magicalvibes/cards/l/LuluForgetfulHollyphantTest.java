package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuluForgetfulHollyphant.class, AirElemental.class, GrizzlyBears.class, Plains.class, Unsummon.class})
class LuluForgetfulHollyphantTest extends BaseCardTest {

    @Test
    void perpetuallyGivesFlyingToTheNextNonFlyingCreatureSpell() {
        LuluForgetfulHollyphant lulu = new LuluForgetfulHollyphant();
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(lulu, bears, secondBears));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent permanent = findPermanent(player1, "Grizzly Bears");
        Permanent secondPermanent = findPermanents(player1, "Grizzly Bears").get(1);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondPermanent, Keyword.FLYING)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
    }

    @Test
    void flyingCreatureDoesNotConsumeTheBoon() {
        LuluForgetfulHollyphant lulu = new LuluForgetfulHollyphant();
        AirElemental flyer = new AirElemental();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(lulu, flyer, bears));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent permanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
    }

    @Test
    void whiteSpecializationGainsTwiceTheNumberOfOtherAttackingFlyers() {
        Permanent lulu = addCreatureReady(player1, new LuluForgetfulHollyphant());
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lulu);
    }

    @Test
    void boonStillAppliesAfterLuluReturnsToHand() {
        harness.setHand(player1, List.of(new LuluForgetfulHollyphant(), new Unsummon(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castAndResolveInstant(player1, 0,
                findPermanent(player1, "Lulu, Forgetful Hollyphant").getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FLYING)).isTrue();
    }

    @Test
    void grantedFlyingSurvivesReturningToHandAndRecasting() {
        harness.setHand(player1, List.of(new LuluForgetfulHollyphant(), new GrizzlyBears(), new Unsummon()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Grizzly Bears").getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FLYING)).isTrue();
    }
}
