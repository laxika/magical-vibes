package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShantottoTacticianMagician.class, GrizzlyBears.class, Hurricane.class, Island.class, Shock.class})
class ShantottoTacticianMagicianTest extends BaseCardTest {

    private Permanent addShantotto(Player player) {
        Permanent permanent = new Permanent(new ShantottoTacticianMagician());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void setDeck(Player player) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(List.of(new Island(), new Island()));
    }

    @Test
    @DisplayName("Casting a one-mana noncreature spell gives +1/+0 and does not draw")
    void cheapNoncreatureSpellBoostsWithoutDrawing() {
        Permanent shantotto = addShantotto(player1);
        setDeck(player1);
        setUpMainPhase(player1);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(shantotto.getPowerModifier()).isEqualTo(1);
        assertThat(shantotto.getToughnessModifier()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Casting a four-mana noncreature spell gives +4/+0 and draws a card")
    void fourManaNoncreatureSpellBoostsAndDraws() {
        Permanent shantotto = addShantotto(player1);
        setDeck(player1);
        setUpMainPhase(player1);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(shantotto.getPowerModifier()).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent shantotto = addShantotto(player1);
        setDeck(player1);
        setUpMainPhase(player1);

        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(shantotto.getPowerModifier()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }
}
