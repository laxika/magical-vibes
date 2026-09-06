package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriceratonCommander.class, FrenziedRaptor.class, GrizzlyBears.class})
class TriceratonCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X 2/2 white Dinosaur Soldier tokens")
    void entersWithXTokens() {
        harness.setHand(player1, List.of(new TriceratonCommander()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Dinosaur Soldier");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.SOLDIER);
        });
    }

    @Test
    @DisplayName("Attacking boosts and grants flying to other Dinosaurs only")
    void attackingBoostsOtherDinosaurs() {
        Permanent commander = addCreatureReady(player1, new TriceratonCommander());
        Permanent dinosaur = addCreatureReady(player1, new FrenziedRaptor());
        Permanent nonDinosaur = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonDinosaur)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonDinosaur)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonDinosaur, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.FLYING)).isFalse();
    }
}
