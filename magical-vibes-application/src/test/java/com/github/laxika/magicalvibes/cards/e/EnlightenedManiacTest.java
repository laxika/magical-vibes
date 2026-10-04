package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnlightenedManiac.class, Murder.class})
class EnlightenedManiacTest extends BaseCardTest {

    @Test
    void enteringBattlefieldCreatesEldraziHorrorToken() {
        harness.setHand(player1, List.of(new EnlightenedManiac()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ELDRAZI))
                .toList();

        assertThat(tokens).singleElement().satisfies(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Eldrazi Horror");
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.HORROR);
        });
    }

    @Test
    void tokenCreationUsesTheStackAndSurvivesRemovalOfTheSource() {
        harness.setHand(player1, List.of(new EnlightenedManiac()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent maniac = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(maniac.getCard().isToken()).isFalse();

        harness.castAndResolveInstant(player2, 0, maniac.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getPower()).isEqualTo(3);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.HORROR);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
}
