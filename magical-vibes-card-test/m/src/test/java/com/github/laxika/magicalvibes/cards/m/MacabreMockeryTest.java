package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NoxiousGroodion;
import com.github.laxika.magicalvibes.cards.e.EssenceCapture;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MacabreMockery.class, NoxiousGroodion.class, EssenceCapture.class})
class MacabreMockeryTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an opponent's creature with haste and +2/+0, then sacrifices it at the next end step")
    void returnsCreatureWithTemporaryRiders() {
        Card target = new NoxiousGroodion();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new MacabreMockery()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Noxious Groodion");
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(returned.getPowerModifier()).isEqualTo(2);
        harness.assertNotInGraveyard(player2, "Noxious Groodion");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Noxious Groodion");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Noxious Groodion");
        harness.assertInGraveyard(player2, "Noxious Groodion");
    }

    @Test
    @DisplayName("Requires a creature card in an opponent's graveyard")
    void rejectsInvalidGraveyardTargets() {
        Card target = new EssenceCapture();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new MacabreMockery()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    @DisplayName("Cannot target a creature card in your own graveyard")
    void rejectsOwnGraveyardTarget() {
        Card target = new NoxiousGroodion();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new MacabreMockery()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("A creature returned during the end step loses haste and its boost at cleanup")
    void endStepReturnLosesTemporaryEffectsBeforeNextEndStep() {
        Card target = new NoxiousGroodion();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new MacabreMockery()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.END_STEP);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Noxious Groodion");
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(returned.getPowerModifier()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Noxious Groodion");
        assertThat(returned.getPowerModifier()).isZero();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Noxious Groodion");
        harness.assertInGraveyard(player2, "Noxious Groodion");
    }

    @Test
    @DisplayName("An opponent's creature that leaves the graveyard before resolution is not returned")
    void targetLeavingGraveyardMakesSpellFizzle() {
        Card target = new NoxiousGroodion();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new MacabreMockery()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Noxious Groodion");
        harness.assertInHand(player2, "Noxious Groodion");
        harness.assertInGraveyard(player1, "Macabre Mockery");
    }
}
