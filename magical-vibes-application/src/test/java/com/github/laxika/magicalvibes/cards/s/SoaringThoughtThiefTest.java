package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoaringThoughtThief.class, Spellbook.class})
class SoaringThoughtThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Rogues get +1/+0 while an opponent has eight cards in their graveyard")
    void boostsRoguesAtGraveyardThreshold() {
        Permanent thief = harness.addToBattlefieldAndReturn(player1, new SoaringThoughtThief());
        Permanent rogue = addReadyCreature(player1, "Test Rogue", 2, 2, CardSubtype.ROGUE);
        Permanent soldier = addReadyCreature(player1, "Test Soldier", 2, 2, CardSubtype.SOLDIER);
        int thiefBasePower = thief.getCard().getPower();

        harness.setGraveyard(player2, graveyardOfSize(7));
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(thiefBasePower);
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);

        harness.setGraveyard(player2, graveyardOfSize(8));
        assertThat(gqs.getEffectivePower(gd, thief)).isEqualTo(thiefBasePower + 1);
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mills each opponent two cards when one or more Rogues attack")
    void millsEachOpponentOnceForMultipleRogues() {
        addReadyCreature(player1, "Soaring Thought-Thief", 1, 3, CardSubtype.ROGUE);
        addReadyCreature(player1, "Test Rogue One", 2, 2, CardSubtype.ROGUE);
        addReadyCreature(player1, "Test Rogue Two", 2, 2, CardSubtype.ROGUE);
        harness.addToBattlefield(player1, new SoaringThoughtThief());
        harness.setLibrary(player2, List.of(new Spellbook(), new Spellbook()));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not mill when only a non-Rogue attacks")
    void doesNotMillForNonRogueAttackers() {
        harness.addToBattlefield(player1, new SoaringThoughtThief());
        addReadyCreature(player1, "Test Soldier", 2, 2, CardSubtype.SOLDIER);
        List<Card> library = List.of(new Spellbook(), new Spellbook());
        harness.setLibrary(player2, library);

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private List<Card> graveyardOfSize(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Spellbook());
        }
        return cards;
    }
}
