package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EffluenceDevourer.class, GrizzlyBears.class, DiabolicEdict.class})
class EffluenceDevourerTest extends BaseCardTest {

    @Test
    void sacrificingAnotherCreatureGrantsTheGraveyardOozeAbility() {
        Card devourer = new EffluenceDevourer();
        harness.setGraveyard(player1, List.of());
        addCreatureReady(player1, devourer);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiabolicEdict(), new DiabolicEdict()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, gd.playerGraveyards.get(player1.getId()).indexOf(devourer));
        harness.passBothPriorities();

        Permanent ooze = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(ooze.getEffectivePower()).isEqualTo(4);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(devourer);
    }

    @Test
    void sacrificingEffluenceDevourerItselfGrantsTheAbilityBeforeItLeaves() {
        Card devourer = new EffluenceDevourer();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        addReadyCreature(player1, devourer);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, gd.playerGraveyards.get(player1.getId()).indexOf(devourer));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(devourer);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getName().equals("Ooze"));
    }

    @Test
    void blitzGrantsHasteDrawsOnDeathAndSacrificesAtEndStep() {
        harness.setHand(player1, List.of(new EffluenceDevourer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent devourer = findPermanent(player1, "Effluence Devourer");
        assertThat(gqs.hasKeyword(gd, devourer, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Effluence Devourer");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
