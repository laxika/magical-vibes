package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BloodcrazedGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OreskosSwiftclaw;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MondoGecko.class, BloodcrazedGoblin.class, GrizzlyBears.class, OreskosSwiftclaw.class})
class MondoGeckoTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card makes Mondo Gecko the chosen color and grants hexproof from it")
    void discardAbilityUsesOneColorForBothEffects() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mondo = harness.addToBattlefieldAndReturn(player1, new MondoGecko());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mondo), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, mondo)).containsExactly(CardColor.RED);
        assertThat(gqs.hasHexproofFromColor(gd, mondo, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Chosen color and hexproof wear off at end of turn")
    void discardAbilityExpiresAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mondo = harness.addToBattlefieldAndReturn(player1, new MondoGecko());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mondo), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, mondo)).containsExactly(CardColor.BLUE);
        assertThat(gqs.hasHexproofFromColor(gd, mondo, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Combat damage draws once for each distinct color among controlled permanents")
    void combatDamageDrawsForEachDistinctControlledColor() {
        Permanent mondo = harness.addToBattlefieldAndReturn(player1, new MondoGecko());
        mondo.setSummoningSick(false);
        mondo.setAttacking(true);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new OreskosSwiftclaw());
        harness.addToBattlefield(player1, new BloodcrazedGoblin());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 4);
    }
}
