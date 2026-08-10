package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DauthiCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with shadow")
    void destroysTargetCreatureWithShadow() {
        addCreatureReady(player1, new DauthiCutthroat());
        Permanent target = addShadowCreature(player2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shadow Creature");
    }

    @Test
    @DisplayName("Cannot target a creature without shadow")
    void cannotTargetCreatureWithoutShadow() {
        addCreatureReady(player1, new DauthiCutthroat());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with shadow");
    }

    private Permanent addShadowCreature(Player player) {
        Card card = new Card();
        card.setName("Shadow Creature");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.SHADOW));
        return addCreatureReady(player, card);
    }
}
