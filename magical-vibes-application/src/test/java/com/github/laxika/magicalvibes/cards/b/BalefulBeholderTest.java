package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalefulBeholder.class, GloriousAnthem.class, GrizzlyBears.class})
class BalefulBeholderTest extends BaseCardTest {

    @Test
    void antimagicConeHasEachOpponentSacrificeAnEnchantment() {
        Permanent firstEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent secondEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBeholder();
        harness.handleListChoice(player1,
                "Antimagic Cone — Each opponent sacrifices an enchantment of their choice");
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstEnchantment.getId(), secondEnchantment.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(firstEnchantment.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(firstEnchantment.getId())
                .contains(secondEnchantment.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void fearRayGrantsMenaceToYourCreaturesUntilEndOfTurn() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBeholder();
        harness.handleListChoice(player1,
                "Fear Ray — Creatures you control gain menace until end of turn");
        harness.passBothPriorities();

        Permanent beholder = findPermanent(player1, "Baleful Beholder");
        assertThat(gqs.hasKeyword(gd, beholder, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MENACE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beholder, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.MENACE)).isFalse();
    }

    private void castBeholder() {
        harness.setHand(player1, List.of(new BalefulBeholder()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
