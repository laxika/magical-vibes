package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UginTheIneffable.class, GrizzlyBears.class, MindStone.class})
class UginTheIneffableTest extends BaseCardTest {

    @Test
    @DisplayName("Colorless spells you cast cost {2} less")
    void reducesColorlessSpells() {
        addReadyUgin();
        harness.setHand(player1, List.of(new MindStone()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mind Stone");
    }

    @Test
    @DisplayName("+1 exiles the top card face down and creates a 2/2 colorless Spirit")
    void plusOneExilesAndCreatesSpirit() {
        Permanent ugin = addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(exiledCard);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(gd.findExiledCard(exiledCard.getId()).faceDown()).isTrue();
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("When the Spirit leaves, its linked exiled card returns to hand")
    void spiritLeavesAndReturnsLinkedCard() {
        addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(exiledCard);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spirit));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    @DisplayName("The Spirit trigger returns its card even after Ugin leaves")
    void spiritTriggerSurvivesUginLeaving() {
        Permanent ugin = addReadyUgin();
        Card exiledCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(exiledCard);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent spirit = findPermanent(player1, "Spirit");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ugin));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spirit));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("−3 destroys a colored permanent")
    void minusThreeDestroysColoredPermanent() {
        addReadyUgin();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("−3 cannot target a colorless permanent")
    void minusThreeRejectsColorlessPermanent() {
        addReadyUgin();
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mindStone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("colored permanent");
    }

    private Permanent addReadyUgin() {
        Permanent ugin = harness.addToBattlefieldAndReturn(player1, new UginTheIneffable());
        ugin.setCounterCount(CounterType.LOYALTY, 4);
        ugin.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ugin;
    }
}
