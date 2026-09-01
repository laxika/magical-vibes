package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamosianRevivalist.class, DefiantFalcon.class, GrizzlyBears.class, HolyDay.class,
        JhovallQueen.class})
class RamosianRevivalistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target Rebel permanent with mana value 5 or less from the graveyard")
    void returnsTargetRebelPermanent() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new DefiantFalcon();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithGraveyardTargets(player1, revivalistIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Defiant Falcon");
        harness.assertNotInGraveyard(player1, "Defiant Falcon");
    }

    @Test
    @DisplayName("Cannot target a non-Rebel card")
    void rejectsNonRebelCard() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-permanent card")
    void rejectsNonPermanentCard() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a Rebel permanent with mana value greater than 5")
    void rejectsRebelWithTooHighManaValue() {
        int revivalistIndex = addReadyRevivalist();
        Card target = new JhovallQueen();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, revivalistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int addReadyRevivalist() {
        harness.addToBattlefield(player1, new RamosianRevivalist());
        Permanent revivalist = findPermanent(player1, "Ramosian Revivalist");
        revivalist.setSummoningSick(false);
        return gd.playerBattlefields.get(player1.getId()).indexOf(revivalist);
    }
}
