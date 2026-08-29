package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LochmereSerpent.class, GrizzlyBears.class, Island.class, Swamp.class})
class LochmereSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Island makes Lochmere Serpent unblockable this turn")
    void islandAbilityMakesSerpentUnblockable() {
        Permanent serpent = addReadySerpent();
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isTrue();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("The Island ability's unblockable effect wears off at cleanup")
    void islandAbilityWearsOffAtCleanup() {
        Permanent serpent = addReadySerpent();
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(serpent.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(serpent.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a Swamp gains life and draws a card")
    void swampAbilityGainsLifeAndDraws() {
        addReadySerpent();
        harness.addToBattlefield(player1, new Swamp());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("The graveyard ability exiles five opposing cards and returns Lochmere Serpent")
    void graveyardAbilityExilesFiveCardsAndReturnsSerpent() {
        LochmereSerpent serpent = new LochmereSerpent();
        List<Card> targets = List.of(
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()
        );
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(serpent));
        harness.setGraveyard(player2, new ArrayList<>(targets));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, targets.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lochmere Serpent");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("The graveyard ability requires five cards in one opponent graveyard")
    void graveyardAbilityRequiresFiveTargets() {
        LochmereSerpent serpent = new LochmereSerpent();
        Card target = new GrizzlyBears();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(serpent));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 5");
    }

    private Permanent addReadySerpent() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new LochmereSerpent());
        serpent.setSummoningSick(false);
        return serpent;
    }

}
