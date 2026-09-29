package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamachalShipsMascot.class, Forest.class, GrizzlyBears.class})
class KamachalShipsMascotTest extends BaseCardTest {

    @Test
    void redAbilityBoostsPowerUntilEndOfTurn() {
        Permanent kamachal = addReadyKamachal(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kamachal.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kamachal.getPowerModifier()).isZero();
    }

    @Test
    void combatDamageCreatesTreasureAndExilesExactManaValueCardForCasting() {
        addReadyKamachal(player1);
        CardInLibrary library = setDamageTwoLibrary();

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        ExiledCardEntry exiled = gd.findExiledCard(library.matchingCard().getId());
        assertThat(exiled).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.nonMatchingCard());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, library.matchingCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(library.matchingCard().getId())).isNull();
    }

    @Test
    void noMatchingManaValueLeavesLibraryUntouchedAfterCreatingTreasure() {
        addReadyKamachal(player1);
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private CardInLibrary setDamageTwoLibrary() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(forest, bears));
        return new CardInLibrary(forest, bears);
    }

    private Permanent addReadyKamachal(Player player) {
        Permanent permanent = new Permanent(new KamachalShipsMascot());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private record CardInLibrary(Forest nonMatchingCard, GrizzlyBears matchingCard) {
    }
}
