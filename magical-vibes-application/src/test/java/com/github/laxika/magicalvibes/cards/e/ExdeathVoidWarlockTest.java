package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.n.NeoExdeathDimensionsEnd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExdeathVoidWarlock.class, NeoExdeathDimensionsEnd.class, GrizzlyBears.class, MindRot.class})
class ExdeathVoidWarlockTest extends BaseCardTest {

    @Test
    void gainsLifeOnEntryAndTransformsAtControllerEndStep() {
        harness.setLife(player1, 10);
        Permanent exdeath = castExdeath();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);

        harness.setGraveyard(player1, permanentCards(6));
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(exdeath.isTransformed()).isTrue();
        assertThat(exdeath.getCard()).isInstanceOf(NeoExdeathDimensionsEnd.class);
    }

    @Test
    void doesNotTransformWithoutSixPermanentCardsInControllerGraveyard() {
        Permanent exdeath = harness.addToBattlefieldAndReturn(player1, new ExdeathVoidWarlock());
        List<Card> graveyard = new ArrayList<>(permanentCards(5));
        graveyard.add(new MindRot());
        harness.setGraveyard(player1, graveyard);
        harness.setGraveyard(player2, permanentCards(6));

        advanceToEndStep(player1);

        assertThat(exdeath.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void backFacePowerEqualsControllerGraveyardPermanentCountAndToughnessIsThree() {
        ExdeathVoidWarlock card = new ExdeathVoidWarlock();
        Permanent exdeath = new Permanent(card);
        exdeath.setCard(card.getBackFaceCard());
        exdeath.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(exdeath);
        List<Card> graveyard = new ArrayList<>(permanentCards(6));
        graveyard.add(new MindRot());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, exdeath)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, exdeath)).isEqualTo(3);
    }

    private Permanent castExdeath() {
        harness.setHand(player1, List.of(new ExdeathVoidWarlock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof ExdeathVoidWarlock)
                .findFirst()
                .orElseThrow();
    }

    private List<Card> permanentCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
