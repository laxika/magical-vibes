package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyclaveAerialist.class, SkyclaveInvader.class, Forest.class, GrizzlyBears.class})
class SkyclaveAerialistTest extends BaseCardTest {

    @Test
    void acceptsTopLandOntoBattlefieldWhenItTransforms() {
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        Permanent aerialist = addAerialist();

        transform(aerialist);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findPermanent(player1, topLand.getName());
        assertThat(land).isNotNull();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void decliningTopLandPutsItIntoHand() {
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        Permanent aerialist = addAerialist();

        transform(aerialist);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(topLand.getId()));
    }

    @Test
    void nonlandTopCardGoesDirectlyToHand() {
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent aerialist = addAerialist();

        transform(aerialist);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent aerialist = addAerialist();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(aerialist), null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(aerialist.isTransformed()).isTrue();
    }

    private Permanent addAerialist() {
        return harness.addToBattlefieldAndReturn(player1, new SkyclaveAerialist());
    }

    private void transform(Permanent aerialist) {
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(aerialist), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
