package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WakeTheReflections;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntangibleVibes.class, GrizzlyBears.class, IntangibleVirtue.class,
        Unsummon.class, WakeTheReflections.class})
class IntangibleVibesTest extends BaseCardTest {

    @Test
    void makesAllCreaturesTokensAndTheyCeaseAfterLeavingBattlefield() {
        harness.addToBattlefield(player1, new IntangibleVibes());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.isToken(gd, bears)).isTrue();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting("name")
                .containsExactly("Grizzly Bears");
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void affectsBothPlayersCreaturesButNotNoncreatures() {
        Permanent vibes = harness.addToBattlefieldAndReturn(player1, new IntangibleVibes());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.isToken(gd, ownBears)).isTrue();
        assertThat(gqs.isToken(gd, opposingBears)).isTrue();
        assertThat(gqs.isToken(gd, vibes)).isFalse();
    }

    @Test
    void creaturesStopBeingTokensWhenVibesLeaves() {
        Permanent vibes = harness.addToBattlefieldAndReturn(player1, new IntangibleVibes());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.isToken(gd, bears)).isTrue();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, vibes);
        harness.passBothPriorities();

        assertThat(gqs.isToken(gd, bears)).isFalse();
        harness.assertInGraveyard(player1, "Intangible Vibes");
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, bears);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void bouncedCreatureCeasesToExistInItsOwnersHand() {
        harness.addToBattlefield(player1, new IntangibleVibes());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void bouncedCreatureBrieflyEntersHandBeforeStateBasedActions() {
        harness.addToBattlefield(player1, new IntangibleVibes());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.getPermanentRemovalService().removePermanentToHand(gd, bears);

        harness.assertInHand(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    void tokenAnthemAppliesToCreaturesMadeTokensByVibes() {
        harness.addToBattlefield(player1, new IntangibleVibes());
        harness.addToBattlefield(player1, new IntangibleVirtue());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void populateCanCopyACreatureMadeATokenByVibes() {
        harness.addToBattlefield(player1, new IntangibleVibes());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WakeTheReflections()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }
}
