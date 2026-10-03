package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

        harness.castAndResolveInstant(player2, 0, agents.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Villain");
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VILLAIN);
                    assertThat(token.hasKeyword(Keyword.MENACE)).isTrue();
                    assertThat(token.isTapped()).isFalse();
                });
        harness.assertInGraveyard(player1, "Agents of HYDRA");
    }

    @Test
    @DisplayName("Simultaneous deaths create a token for each dying creature's controller")
    void simultaneousDeathsCreateTokensForBothControllers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AgentsOfHYDRA());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AgentsOfHYDRA());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Agents of HYDRA");
        harness.assertInGraveyard(player2, "Agents of HYDRA");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(token -> assertThat(token.getCard().isToken()).isTrue());
    }
}
