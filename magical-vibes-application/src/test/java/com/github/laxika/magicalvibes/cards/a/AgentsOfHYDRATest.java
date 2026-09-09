package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentsOfHYDRA.class, Shock.class})
class AgentsOfHYDRATest extends BaseCardTest {

    @Test
    @DisplayName("When Agents of HYDRA dies, it creates a 2/1 black Villain token with menace")
    void deathCreatesVillainTokenWithMenace() {
        Permanent agents = harness.addToBattlefieldAndReturn(player1, new AgentsOfHYDRA());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, agents.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Villain");
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
                });
        harness.assertInGraveyard(player1, "Agents of HYDRA");
    }
}
