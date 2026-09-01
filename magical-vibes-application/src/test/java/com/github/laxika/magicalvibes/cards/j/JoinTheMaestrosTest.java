package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoinTheMaestros.class, GrizzlyBears.class, LlanowarElves.class})
class JoinTheMaestrosTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/3 black Ogre Warrior token")
    void createsOgreWarriorToken() {
        harness.setHand(player1, List.of(new JoinTheMaestros()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.get(0);
        assertThat(token.getCard().getPower()).isEqualTo(4);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.OGRE, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Casualty copies Join the Maestros")
    void casualtyCopiesSpell() {
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JoinTheMaestros()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Cannot pay casualty with a creature below the required power")
    void rejectsUnderpoweredCasualtyCreature() {
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new JoinTheMaestros()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
