package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallOfTheConclave.class})
class CallOfTheConclaveTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creates a 3/3 green Centaur token under the caster's control")
    void resolvingCreatesCentaur() {
        harness.setHand(player1, List.of(new CallOfTheConclave()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());

        List<Permanent> centaurs = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CENTAUR))
                .toList();
        assertThat(centaurs).hasSize(1);
        Permanent token = centaurs.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Call of the Conclave");
    }

    @Test
    @DisplayName("Creates an untapped green creature only when the spell resolves")
    void tokenCharacteristicsAndCreationTiming() {
        harness.setHand(player1, List.of(new CallOfTheConclave()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CENTAUR);
        assertThat(token.isTapped()).isFalse();
    }
}
