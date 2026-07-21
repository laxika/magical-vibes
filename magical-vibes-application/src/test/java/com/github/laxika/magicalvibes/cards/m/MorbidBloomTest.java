package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MorbidBloomTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);   // {B}
        harness.addMana(player1, ManaColor.GREEN, 1);    // {G}
        harness.addMana(player1, ManaColor.COLORLESS, 4); // {4}
    }

    private long saprolingCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .count();
    }

    @Test
    @DisplayName("Exiles the target creature card and creates Saprolings equal to its toughness")
    void exilesAndCreatesSaprolingsEqualToToughness() {
        Card bears = new GrizzlyBears(); // 2/2 -> 2 Saprolings
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new MorbidBloom()));
        giveMana();

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        assertThat(saprolingCount()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Saproling"))
                .allMatch(p -> p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getSubtypes().contains(CardSubtype.SAPROLING));
    }

    @Test
    @DisplayName("Token count scales with the exiled card's toughness (3/3 -> 3 Saprolings)")
    void tokenCountScalesWithToughness() {
        Card giant = new HillGiant(); // 3/3 -> 3 Saprolings
        harness.setGraveyard(player1, new ArrayList<>(List.of(giant)));
        harness.setHand(player1, List.of(new MorbidBloom()));
        giveMana();

        harness.castSorcery(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(saprolingCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a non-creature card")
    void rejectsNonCreatureTarget() {
        Card cancel = new Cancel();
        harness.setGraveyard(player2, new ArrayList<>(List.of(cancel)));
        harness.setHand(player1, List.of(new MorbidBloom()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, cancel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles with no Saprolings if the target leaves the graveyard before resolution")
    void fizzlesIfTargetRemoved() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new MorbidBloom()));
        giveMana();

        harness.castSorcery(player1, 0, bears.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(saprolingCount()).isZero();
    }
}
