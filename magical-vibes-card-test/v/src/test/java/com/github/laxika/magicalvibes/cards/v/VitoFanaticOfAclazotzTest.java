package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FanaticalOffering;
import com.github.laxika.magicalvibes.cards.t.ThousandMoonsInfantry;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VitoFanaticOfAclazotz.class, ThousandMoonsInfantry.class, FanaticalOffering.class})
class VitoFanaticOfAclazotzTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice resolutions gain life, drain opponents, then create a Vampire Demon")
    void sacrificeResolutionsProgressThroughModes() {
        addCreatureReady(player1, new VitoFanaticOfAclazotz());
        Permanent first = addCreatureReady(player1, new ThousandMoonsInfantry());
        Permanent second = addCreatureReady(player1, new ThousandMoonsInfantry());
        Permanent third = addCreatureReady(player1, new ThousandMoonsInfantry());
        int playerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        sacrifice(first);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);

        sacrifice(second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);

        sacrifice(third);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);

        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
        assertThat(token.getCard().hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void sacrificingAnArtifactTokenAsACastingCostAlsoTriggersVito() {
        addCreatureReady(player1, new VitoFanaticOfAclazotz());
        Permanent creature = addCreatureReady(player1, new ThousandMoonsInfantry());
        harness.setLibrary(player1, List.of(new ThousandMoonsInfantry(), new ThousandMoonsInfantry(),
                new ThousandMoonsInfantry(), new ThousandMoonsInfantry()));
        harness.setHand(player1, List.of(new FanaticalOffering(), new FanaticalOffering()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        Permanent map = findPermanent(player1, "Map");
        harness.castInstantWithSacrifice(player1, 0, null, map.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Map")).isEqualTo(1);
    }

    @Test
    void fourthAndLaterResolutionsHaveNoEffect() {
        addCreatureReady(player1, new VitoFanaticOfAclazotz());
        for (int i = 0; i < 5; i++) {
            sacrifice(addCreatureReady(player1, new ThousandMoonsInfantry()));
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Vampire Demon")).isEqualTo(1);
    }

    @Test
    void queuedTriggersChooseTheirEffectWhenTheyResolve() {
        addCreatureReady(player1, new VitoFanaticOfAclazotz());
        for (int i = 0; i < 3; i++) {
            enqueueSacrifice(player1, addCreatureReady(player1, new ThousandMoonsInfantry()));
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Vampire Demon")).isEqualTo(1);
    }

    @Test
    void sacrificingVitoDoesNotTriggerItsOwnAbility() {
        sacrifice(addCreatureReady(player1, new VitoFanaticOfAclazotz()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Vampire Demon")).isZero();
    }

    @Test
    void opponentsSacrificesDoNotTriggerVito() {
        addCreatureReady(player1, new VitoFanaticOfAclazotz());
        enqueueSacrifice(player2, addCreatureReady(player2, new ThousandMoonsInfantry()));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void pendingAbilityStillResolvesAfterVitoIsSacrificed() {
        Permanent vito = addCreatureReady(player1, new VitoFanaticOfAclazotz());
        enqueueSacrifice(player1, addCreatureReady(player1, new ThousandMoonsInfantry()));
        enqueueSacrifice(player1, vito);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(countPermanents(player1, "Vito, Fanatic of Aclazotz")).isZero();
    }

    @Test
    void resolutionCountResetsOnTheOpponentsTurn() {
        addCreatureReady(player1, new VitoFanaticOfAclazotz());
        sacrifice(addCreatureReady(player1, new ThousandMoonsInfantry()));
        sacrifice(addCreatureReady(player1, new ThousandMoonsInfantry()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        sacrifice(addCreatureReady(player1, new ThousandMoonsInfantry()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Vampire Demon")).isZero();
    }

    private void sacrifice(Permanent permanent) {
        enqueueSacrifice(player1, permanent);
        resolveAllTriggers();
    }

    private void enqueueSacrifice(Player player, Permanent permanent) {
        harness.inMutationScope(() -> {
            assertThat(gd.playerBattlefields.get(player.getId()).remove(permanent)).isTrue();
            gd.playerGraveyards.get(player.getId()).add(permanent.getCard());
            harness.getTriggerCollectionService()
                    .checkAllyPermanentSacrificedTriggers(gd, player.getId(), permanent.getCard());
        });
    }
}
