package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Barrowgoyf.class, Forest.class, GrizzlyBears.class, LavaSpike.class, Millstone.class,
        Ornithopter.class, Shock.class})
class BarrowgoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness count distinct card types in all graveyards")
    void countsDistinctCardTypesInAllGraveyards() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.setGraveyard(player2, List.of(new LavaSpike(), new Ornithopter()));

        assertThat(gqs.getEffectivePower(gd, barrowgoyf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, barrowgoyf)).isEqualTo(6);
    }

    @Test
    @DisplayName("Combat damage may mill that many cards and return milled creatures")
    void millsCombatDamageAndReturnsMilledCreatures() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));

        GrizzlyBears milledCreature = new GrizzlyBears();
        Card milledNoncreature = new Shock();
        harness.setLibrary(player1, List.of(milledCreature, milledNoncreature, new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(milledCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(milledNoncreature)
                .hasSize(5);
    }

    @Test
    @DisplayName("Declining the mill leaves the library unchanged")
    void mayDeclineToMill() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Shock", "Millstone");
    }
}
