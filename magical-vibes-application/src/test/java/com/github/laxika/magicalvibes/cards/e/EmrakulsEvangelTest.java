package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrownyardBehemoth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmrakulsEvangelTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices the Evangel and chosen non-Eldrazi creatures to create one token each")
    void sacrificesChosenCreaturesAndCreatesTokens() {
        addCreatureReady(player1, new EmrakulsEvangel());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent eldrazi = addCreatureReady(player1, new DrownyardBehemoth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ActivatedAbilityCostChoice.class);
        assertThat(choice.validIds()).containsExactly(bear.getId(), otherBear.getId());

        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Horror")).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getColor()).isNull();
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.HORROR);
                    assertThat(token.getEffectivePower()).isEqualTo(3);
                    assertThat(token.getEffectiveToughness()).isEqualTo(2);
                });
        harness.assertInGraveyard(player1, "Emrakul's Evangel");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherBear, eldrazi);
    }

    @Test
    @DisplayName("Creates one token when no other creature is sacrificed")
    void createsTokenForTheEvangelAlone() {
        addCreatureReady(player1, new EmrakulsEvangel());

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Eldrazi Horror")).hasSize(1);
        harness.assertInGraveyard(player1, "Emrakul's Evangel");
    }
}
