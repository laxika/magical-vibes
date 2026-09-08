package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GretchenTitchwillow.class, Forest.class, Island.class})
class GretchenTitchwillowTest extends BaseCardTest {

    @Test
    void drawsCardAndMayPutLandOntoBattlefieldUntapped() {
        addReadyGretchen(player1);
        Card forest = new Forest();
        Card island = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(island);
        harness.setHand(player1, List.of(forest));
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        Permanent forestPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(forestPermanent.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard() == island)).isFalse();
    }

    @Test
    void decliningStillDrawsCardAndPutsNoLandOntoBattlefield() {
        addReadyGretchen(player1);
        Card forest = new Forest();
        Card island = new Island();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(island);
        harness.setHand(player1, List.of(forest));
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, island);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard() == forest)).isTrue();
    }

    private Permanent addReadyGretchen(Player player) {
        Permanent gretchen = new Permanent(new GretchenTitchwillow());
        gretchen.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(gretchen);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gretchen;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
