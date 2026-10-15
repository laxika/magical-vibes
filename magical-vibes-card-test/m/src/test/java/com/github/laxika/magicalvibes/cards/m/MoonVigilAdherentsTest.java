package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonVigilAdherents.class, Plains.class})
class MoonVigilAdherentsTest extends BaseCardTest {

    @Test
    @DisplayName("Survives entering the battlefield as the controller's only creature")
    void survivesResolvingAsOnlyCreature() {
        harness.setHand(player1, List.of(new MoonVigilAdherents()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Moon-Vigil Adherents");
        Permanent adherents = findPermanent(player1, "Moon-Vigil Adherents");
        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts itself when it is your only creature")
    void countsItself() {
        Permanent adherents = addAdherents(player1);

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 for each creature you control and creature card in your graveyard")
    void countsControlledCreaturesAndGraveyardCreatures() {
        Permanent adherents = addAdherents(player1);
        harness.addToBattlefield(player1, new MoonVigilAdherents());
        harness.addToBattlefield(player1, new MoonVigilAdherents());
        harness.setGraveyard(player1, List.of(new MoonVigilAdherents(), new MoonVigilAdherents(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not count opposing creatures or noncreature cards")
    void ignoresOpposingCreaturesAndNoncreatureCards() {
        Permanent adherents = addAdherents(player1);
        harness.addToBattlefield(player2, new MoonVigilAdherents());

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new Plains());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(1);
    }

    @Test
    @DisplayName("Updates as creatures enter your graveyard")
    void updatesWithGraveyardChanges() {
        Permanent adherents = addAdherents(player1);

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new MoonVigilAdherents());

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count creature cards in the opponent's graveyard")
    void ignoresOpposingGraveyard() {
        Permanent adherents = addAdherents(player1);
        harness.setGraveyard(player2, List.of(new MoonVigilAdherents(), new MoonVigilAdherents()));

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(1);
    }

    @Test
    @DisplayName("Shrinks when creature cards leave the graveyard and creatures leave the battlefield")
    void updatesWhenCountsDecrease() {
        Permanent adherents = addAdherents(player1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MoonVigilAdherents());
        harness.addToBattlefield(player1, new Plains());
        harness.setGraveyard(player1, List.of(new MoonVigilAdherents()));

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(3);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(other);
        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the current controller's creatures and graveyard after changing control")
    void updatesAfterControlChange() {
        Permanent adherents = addAdherents(player1);
        harness.setGraveyard(player1, List.of(new MoonVigilAdherents()));
        harness.addToBattlefield(player2, new MoonVigilAdherents());
        harness.setGraveyard(player2, List.of(new MoonVigilAdherents(), new MoonVigilAdherents()));

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(adherents);
        gd.playerBattlefields.get(player2.getId()).add(adherents);

        assertThat(gqs.getEffectivePower(gd, adherents)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adherents)).isEqualTo(4);
    }

    private Permanent addAdherents(Player player) {
        return addCreatureReady(player, new MoonVigilAdherents());
    }
}
