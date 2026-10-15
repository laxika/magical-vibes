package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BafflingEnd;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MartyrOfDusk.class, WrathOfGod.class, BafflingEnd.class})
class MartyrOfDuskTest extends BaseCardTest {

    @Test
    void deathTriggerCreatesWhiteVampireTokenWithLifelink() {
        harness.addToBattlefield(player1, new MartyrOfDusk());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Martyr of Dusk");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Vampire");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    void deathTriggerCreatesTokenForMartyrController() {
        harness.addToBattlefield(player2, new MartyrOfDusk());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Vampire")).hasSize(1);
        assertThat(findPermanents(player1, "Vampire")).isEmpty();
    }

    @Test
    void simultaneousDeathsCreateOneTokenPerMartyrAfterTheSweeperResolves() {
        harness.addToBattlefield(player1, new MartyrOfDusk());
        harness.addToBattlefield(player1, new MartyrOfDusk());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanents(player1, "Vampire")).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exileDoesNotCreateAVampireToken() {
        harness.addToBattlefield(player2, new MartyrOfDusk());
        harness.setHand(player1, List.of(new BafflingEnd()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, harness.getPermanentId(player2, "Martyr of Dusk"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Martyr of Dusk");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Martyr of Dusk"));
        assertThat(findPermanents(player2, "Vampire")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
