package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.f.FamiliarGround;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntingMisery.class, BenalishInfantry.class, FamiliarGround.class})
class HauntingMiseryTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Exiling creature cards sets X and deals that much damage to target player")
    void dealsDamageEqualToExiledCreatureCount() {
        harness.setGraveyard(player1, List.of(new BenalishInfantry(), new BenalishInfantry()));
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        harness.setLife(player2, 20);
        harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of(0, 1));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed(NicolBolasPlaneswalker.class)
    @DisplayName("Can target a planeswalker and deals damage equal to exiled creature count")
    void dealsDamageToPlaneswalkerEqualToExiledCreatureCount() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(new BenalishInfantry(), new BenalishInfantry()));
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        harness.castInstantWithMultipleGraveyardExile(player1, 0, planeswalker.getId(), List.of(0, 1));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiling zero cards deals no damage")
    void zeroExilesDealsNoDamage() {
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        harness.setLife(player2, 20);
        harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can cast for zero with an empty graveyard")
    void canCastForZeroWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        harness.setLife(player2, 20);
        harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use duplicate graveyard indices")
    void cannotUseDuplicateGraveyardIndices() {
        harness.setGraveyard(player1, List.of(new BenalishInfantry(), new BenalishInfantry()));
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        assertThatThrownBy(() ->
                harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of(0, 0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    @DisplayName("Non-creature cards can't be exiled for the additional cost")
    void cannotExileNonCreatureCards() {
        harness.setGraveyard(player1, List.of(new FamiliarGround()));
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        assertThatThrownBy(() ->
                harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
