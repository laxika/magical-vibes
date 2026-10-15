package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.h.HeartlessAct;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaysquadMarshal.class, HeartlessAct.class})
class DaysquadMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier token when it enters")
    void createsSoldierTokenOnEnter() {
        harness.setHand(player1, List.of(new DaysquadMarshal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Creates exactly one untapped 1/1 white Human Soldier for its controller")
    void createsHumanSoldierWithOracleCharacteristics() {
        harness.enterBattlefieldAndReturn(player2, new DaysquadMarshal());
        resolveAllTriggers();

        var tokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        var token = tokens.getFirst();
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Entry trigger creates its token even if Marshal is destroyed in response")
    void createsTokenAfterSourceLeavesBattlefield() {
        var marshal = harness.enterBattlefieldAndReturn(player1, new DaysquadMarshal());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        harness.setHand(player1, List.of(new HeartlessAct()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, marshal.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Daysquad Marshal");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }
}
