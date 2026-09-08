package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishEulogist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AshesOfTheFallenTest extends BaseCardTest {

    @Test
    void chosenTypeAppliesToCreatureCardsInYourGraveyard() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player1, List.of(bearCard()));
        castAndChooseElf();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void chosenTypeDoesNotApplyToCreatureCardsInHand() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setHand(player1, List.of(bearCard()));
        castAndChooseElf();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void chosenTypeDoesNotApplyToAnOpponentsGraveyard() {
        harness.addToBattlefield(player1, new ElvishEulogist());
        harness.setGraveyard(player2, List.of(bearCard()));
        castAndChooseElf();

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    private void castAndChooseElf() {
        harness.setHand(player1, List.of(new AshesOfTheFallen()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());
    }

    private static Card bearCard() {
        Card card = new Card();
        card.setName("Grizzly Bears");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setSubtypes(List.of(CardSubtype.BEAR));
        return card;
    }
}
