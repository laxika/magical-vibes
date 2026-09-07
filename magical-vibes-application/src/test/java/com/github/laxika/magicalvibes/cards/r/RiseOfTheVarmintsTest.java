package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiseOfTheVarmints.class, GrizzlyBears.class, LightningBolt.class})
class RiseOfTheVarmintsTest extends BaseCardTest {

    @Test
    void createsOneVarmintPerCreatureCardInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RiseOfTheVarmints()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).filteredOn(p -> p.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Varmint");
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VARMINT);
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    void createsNoTokensWithNoCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of(new RiseOfTheVarmints()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .isEmpty();
    }
}
