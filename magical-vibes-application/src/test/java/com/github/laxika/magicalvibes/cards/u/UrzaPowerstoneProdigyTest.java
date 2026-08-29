package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UrzaPowerstoneProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and discards a card when activated")
    void drawsThenDiscards() {
        addUrza();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Spellbook");
        assertThat(countPermanents(player1, "Powerstone")).isZero();
    }

    @Test
    @DisplayName("Discarding an artifact creates a tapped Powerstone token")
    void artifactDiscardCreatesTappedPowerstone() {
        addUrza();
        harness.setHand(player1, List.of(new Spellbook()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        List<Permanent> powerstones = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.POWERSTONE))
                .toList();
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
        assertThat(powerstones.getFirst().getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("Creates at most one Powerstone from artifact discards each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent urza = addUrza();
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        lootAndDiscard(0, urza);
        lootAndDiscard(0, urza);

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
    }

    private Permanent addUrza() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaPowerstoneProdigy());
        urza.setSummoningSick(false);
        return urza;
    }

    private void lootAndDiscard(int discardIndex, Permanent urza) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, discardIndex);
        harness.passBothPriorities();
        urza.untap();
    }
}
