package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HanweirMilitiaCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms at upkeep with four creatures")
    void transformsAtUpkeepWithFourCreatures() {
        Permanent captain = addCaptainReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(captain.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at upkeep with fewer than four creatures")
    void doesNotTransformWithFewerThanFourCreatures() {
        Permanent captain = addCaptainReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(captain.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cult Leader has power and toughness equal to controlled creatures")
    void cultLeaderScalesWithControlledCreatures() {
        Permanent leader = addTransformedCaptain(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cult Leader creates a multicolored Human Cleric token at its controller's end step")
    void cultLeaderCreatesHumanClericTokenAtEndStep() {
        Permanent leader = addTransformedCaptain(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CLERIC);
        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leader)).isEqualTo(2);
    }

    private Permanent addCaptainReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new HanweirMilitiaCaptain());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addTransformedCaptain(Player player) {
        HanweirMilitiaCaptain card = new HanweirMilitiaCaptain();
        Permanent permanent = new Permanent(card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

}
