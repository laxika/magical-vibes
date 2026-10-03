package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrestedHerdcaller.class})
class CrestedHerdcallerTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldCreatesATramplingDinosaurToken() {
        harness.setHand(player1, List.of(new CrestedHerdcaller()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Dinosaur");
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DINOSAUR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    void tokenIsCreatedOnlyWhenTheEnterTriggerResolves() {
        harness.setHand(player1, List.of(new CrestedHerdcaller()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crested Herdcaller");
        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
