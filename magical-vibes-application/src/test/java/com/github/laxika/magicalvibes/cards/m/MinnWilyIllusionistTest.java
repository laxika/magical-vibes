package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinnWilyIllusionist.class, BlindPhantasm.class, GrizzlyBears.class, Forest.class, Shock.class})
class MinnWilyIllusionistTest extends BaseCardTest {

    @Test
    void secondDrawCreatesIllusionsThatScaleWithOtherIllusions() {
        harness.addToBattlefieldAndReturn(player1, new MinnWilyIllusionist());
        Permanent blindPhantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        assertThat(blindPhantasm.getCard().getSubtypes()).contains(CardSubtype.ILLUSION);
        addCardsToDeck(2);

        draw();
        assertThat(gd.stack).isEmpty();
        draw();
        resolveAllTriggers();

        List<Permanent> illusions = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(illusions).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, illusions.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, illusions.getFirst())).isEqualTo(1);
    }

    @Test
    void dyingIllusionMayPutPermanentFromHandWithManaValueUpToItsPower() {
        harness.addToBattlefieldAndReturn(player1, new MinnWilyIllusionist());
        addCardsToDeck(2);
        draw();
        draw();
        resolveAllTriggers();

        Permanent illusion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        killWithShock(illusion);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandCardChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addCardsToDeck(int count) {
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        }
    }

    private void draw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
