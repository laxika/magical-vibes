package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverDeputy.class, Forest.class, HostileDesert.class, GrizzlyBears.class})
class SilverDeputyTest extends BaseCardTest {

    @Test
    void etbSearchesForBasicLandOrDesertAndPutsItOnTop() {
        Card nonMatch = new GrizzlyBears();
        Card forest = new Forest();
        Card desert = new HostileDesert();
        setLibrary(nonMatch, forest, desert);

        castSilverDeputy();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, desert);
        assertThat(search.params().cards()).doesNotContain(nonMatch);

        int desertIndex = search.params().cards().indexOf(desert);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(desertIndex));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(desert);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(desert, nonMatch, forest);
    }

    @Test
    void etbSearchMayBeDeclined() {
        Card forest = new Forest();
        setLibrary(forest);

        castSilverDeputy();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void activatedAbilityTapsAndBoostsControlledCreatureUntilEndOfTurn() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(deputy), null, target.getId());
        harness.passBothPriorities();

        assertThat(deputy.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    void activatedAbilityCannotTargetOpponentCreature() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityRequiresSorcerySpeed() {
        Permanent deputy = addCreatureReady(player1, new SilverDeputy());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(deputy), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSilverDeputy() {
        harness.setHand(player1, List.of(new SilverDeputy()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(Card... cards) {
        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player1.getId()).clear();
        gameData.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
