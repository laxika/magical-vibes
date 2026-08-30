package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PromptoArgentum.class, Shock.class, Hurricane.class, GrizzlyBears.class})
class PromptoArgentumTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell with less than four mana spent does not create a Treasure")
    void cheapNoncreatureSpellDoesNotCreateTreasure() {
        addCreatureReady(player1, new PromptoArgentum());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(treasureTokens()).isZero();
    }

    @Test
    @DisplayName("A noncreature spell with at least four mana spent creates a Treasure")
    void expensiveNoncreatureSpellCreatesTreasure() {
        addCreatureReady(player1, new PromptoArgentum());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(treasureTokens()).isOne();
    }

    @Test
    @DisplayName("A creature spell does not create a Treasure")
    void creatureSpellDoesNotCreateTreasure() {
        addCreatureReady(player1, new PromptoArgentum());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(treasureTokens()).isZero();
    }

    private long treasureTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .count();
    }

    private void setUpMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
