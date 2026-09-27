package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkWingsBringYourDownfall.class, GrizzlyBears.class, Damnation.class})
class DarkWingsBringYourDownfallTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking 5/5 Demon token")
    void attackCreatesDemonToken() {
        harness.addToBattlefield(player1, new DarkWingsBringYourDownfall());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent demon = findPermanents(player1, "Demon").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(demon.isTapped()).isTrue();
        assertThat(demon.isAttackedThisTurn()).isTrue();
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Abandons at the end step after two creatures you control die")
    void abandonsAfterTwoControlledCreaturesDie() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new DarkWingsBringYourDownfall());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyCreaturesWithDamnation();
        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
    }

    @Test
    @DisplayName("Opponent creature deaths do not satisfy the abandonment condition")
    void ignoresOpponentCreatureDeaths() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new DarkWingsBringYourDownfall());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyCreaturesWithDamnation();
        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
    }

    private void destroyCreaturesWithDamnation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Damnation()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
