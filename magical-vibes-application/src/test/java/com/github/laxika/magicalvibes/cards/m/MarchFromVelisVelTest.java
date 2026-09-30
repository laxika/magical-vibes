package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchFromVelisVel.class, Desert.class, Forest.class, GrizzlyBears.class})
class MarchFromVelisVelTest extends BaseCardTest {

    @Test
    void copiesControlledDesertsWithHasteAndLeavesOtherLandsAndOpponentsLandsAlone() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentDesert = harness.addToBattlefieldAndReturn(player2, new Desert());

        castAndChooseDesert(target);

        assertThat(desert.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(desert.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(desert.getCard().getPower()).isEqualTo(2);
        assertThat(desert.getCard().getToughness()).isEqualTo(2);
        assertThat(forest.getCard().getName()).isEqualTo("Forest");
        assertThat(opponentDesert.getCard().getName()).isEqualTo("Desert");
    }

    @Test
    void copiesRevertAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());

        castAndChooseDesert(target);
        assertThat(desert.getCard().getName()).isEqualTo("Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(desert.getCard().getName()).isEqualTo("Desert");
        assertThat(desert.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    void flashbackCopiesTheChosenLandsAndExilesTheSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        harness.setGraveyard(player1, List.of(new MarchFromVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyardTargeting(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DESERT");

        assertThat(desert.getCard().getName()).isEqualTo("Grizzly Bears");
        harness.assertNotInGraveyard(player1, "March from Velis Vel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("March from Velis Vel"));
    }

    @Test
    void cannotTargetAnOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchFromVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .hasMessageContaining("creature you control");
    }

    private void castAndChooseDesert(Permanent target) {
        harness.setHand(player1, List.of(new MarchFromVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DESERT");
    }
}
