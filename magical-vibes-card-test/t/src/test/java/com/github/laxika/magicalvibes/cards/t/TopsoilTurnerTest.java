package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronrootTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TopsoilTurner.class, Forest.class, IronrootTreefolk.class, GrizzlyBears.class})
class TopsoilTurnerTest extends BaseCardTest {

    @Test
    void perpetuallyGrantsTheManaAbilityToForestsAndTreefolkInHand() {
        Forest forest = new Forest();
        IronrootTreefolk treefolk = new IronrootTreefolk();
        GrizzlyBears nonmatching = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, treefolk, nonmatching));

        harness.enterBattlefieldAndReturn(player1, new TopsoilTurner());
        resolveAllTriggers();

        harness.setHand(player1, List.of());
        Permanent forestPermanent = harness.enterBattlefieldAndReturn(player1, forest);
        Permanent treefolkPermanent = harness.enterBattlefieldAndReturn(player1, treefolk);
        Permanent nonmatchingPermanent = harness.enterBattlefieldAndReturn(player1, nonmatching);
        treefolkPermanent.setSummoningSick(false);

        assertThat(gs.getEffectiveActivatedAbilities(gd, forestPermanent)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, treefolkPermanent)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, nonmatchingPermanent)).isEmpty();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(forestPermanent), null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(forestPermanent.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(treefolkPermanent), null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(treefolkPermanent.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesTheHandAtResolutionAndDoesNotAffectOpponentsOrExistingPermanents() {
        Forest departed = new Forest();
        Forest arriving = new Forest();
        Forest opponent = new Forest();
        Permanent existing = harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(departed));
        harness.setHand(player2, List.of(opponent));

        harness.enterBattlefieldAndReturn(player1, new TopsoilTurner());
        harness.setHand(player1, List.of(arriving));
        resolveAllTriggers();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent affected = harness.enterBattlefieldAndReturn(player1, arriving);
        Permanent unaffected = harness.enterBattlefieldAndReturn(player1, departed);
        Permanent opponentPermanent = harness.enterBattlefieldAndReturn(player2, opponent);

        assertThat(gs.getEffectiveActivatedAbilities(gd, existing)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, unaffected)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opponentPermanent)).isEmpty();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(affected), null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void grantedTreefolkManaAbilityRequiresSummoningSicknessToEnd() {
        IronrootTreefolk treefolk = new IronrootTreefolk();
        harness.setHand(player1, List.of(treefolk));
        harness.enterBattlefieldAndReturn(player1, new TopsoilTurner());
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, treefolk);
        permanent.setSummoningSick(true);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(permanent);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(permanent.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        permanent.setSummoningSick(false);
        harness.activateAbility(player1, index, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void abilityPersistsAfterReturningToHandAndAfterTurnerLeaves() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        Permanent turner = harness.enterBattlefieldAndReturn(player1, new TopsoilTurner());
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        Permanent firstEntry = harness.enterBattlefieldAndReturn(player1, forest);

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, firstEntry);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, turner);
        });
        harness.playLand(player1, 0);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.activateAbility(player1, 0, null, null);
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyHandTriggerDoesNotAffectCardsAddedAfterResolution() {
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new TopsoilTurner());
        resolveAllTriggers();

        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        harness.playLand(player1, 0);
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getLast();

        assertThat(gs.getEffectiveActivatedAbilities(gd, permanent)).isEmpty();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(permanent));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
