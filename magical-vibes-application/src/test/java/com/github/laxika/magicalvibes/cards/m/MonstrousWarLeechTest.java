package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArgivianPhalanx;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CutDown;
import com.github.laxika.magicalvibes.cards.t.TributeToUrborg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonstrousWarLeech.class, Forest.class, CutDown.class, PhyrexianRager.class,
        TributeToUrborg.class, ArgivianPhalanx.class})
class MonstrousWarLeechTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the greatest mana value in your graveyard")
    void powerAndToughnessUseGreatestOwnGraveyardManaValue() {
        harness.setGraveyard(player1, List.of(new CutDown(), new PhyrexianRager(), new TributeToUrborg()));
        harness.setGraveyard(player2, List.of(new ArgivianPhalanx()));

        Permanent leech = harness.addToBattlefieldAndReturn(player1, new MonstrousWarLeech());

        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(3);
    }

    @Test
    @DisplayName("Unkicked entry does not mill")
    void unkickedEntryDoesNotMill() {
        harness.setHand(player1, List.of(new MonstrousWarLeech()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Kicked entry mills four cards")
    void kickedEntryMillsFourCards() {
        harness.setHand(player1, List.of(new MonstrousWarLeech()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Forest"))
                .hasSize(4);
    }

    @Test
    @DisplayName("Kicked entry mills before state-based actions and survives with the milled mana value")
    void kickedEntrySurvivesWithInitiallyEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new MonstrousWarLeech()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new MonstrousWarLeech(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Monstrous War-Leech");
        Permanent leech = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MonstrousWarLeech)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(4);
    }

    @Test
    @DisplayName("Kicked entry mills all remaining cards when fewer than four remain")
    void kickedEntryWithShortLibrary() {
        harness.setGraveyard(player1, List.of(new MonstrousWarLeech()));
        harness.setHand(player1, List.of(new MonstrousWarLeech()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Monstrous War-Leech");
    }

    @Test
    @DisplayName("Power and toughness update when the greatest graveyard mana value changes")
    void powerAndToughnessUpdateWithGraveyard() {
        harness.setGraveyard(player1, List.of(new MonstrousWarLeech()));
        Permanent leech = harness.addToBattlefieldAndReturn(player1, new MonstrousWarLeech());
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(4);

        harness.setGraveyard(player1, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, leech)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, leech)).isZero();

        harness.setGraveyard(player1, List.of());
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Monstrous War-Leech");
        harness.assertInGraveyard(player1, "Monstrous War-Leech");
    }
}
