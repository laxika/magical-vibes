package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElvishEulogistTest extends BaseCardTest {

    private static Card elfCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.ELF));
        return card;
    }

    private static Card nonElfCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        return card;
    }

    @Test
    @DisplayName("Sacrificing gains 1 life per Elf card in graveyard, counting itself")
    void gainsLifePerElfInGraveyard() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(elfCard("Wren's Run Vanquisher"), elfCard("Imperious Perfect"), nonElfCard("Grizzly Bears")));

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // 2 Elf cards already in the graveyard + the sacrificed Eulogist itself = 3 life.
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(findEulogist(gd, player1)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Elvish Eulogist"));
    }

    @Test
    @DisplayName("With no other Elf cards, sacrificing still gains 1 life from itself")
    void gainsOneLifeFromItselfWhenGraveyardEmpty() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(nonElfCard("Grizzly Bears")));

        int lifeBefore = harness.getGameData().getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    private static com.github.laxika.magicalvibes.model.Permanent findEulogist(GameData gd, Player player) {
        return gd.playerBattlefields.getOrDefault(player.getId(), List.of()).stream()
                .filter(p -> p.getCard() instanceof ElvishEulogist)
                .findFirst()
                .orElse(null);
    }
}
