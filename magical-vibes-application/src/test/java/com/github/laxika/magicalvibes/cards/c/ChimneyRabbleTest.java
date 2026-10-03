package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RebelSalvo;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChimneyRabble.class, RebelSalvo.class})
class ChimneyRabbleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 red Phyrexian Goblin token")
    void etbCreatesPhyrexianGoblinToken() {
        harness.setHand(player1, List.of(new ChimneyRabble()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Phyrexian Goblin");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    void canAttackOnTheTurnItEnters() {
        harness.setHand(player1, List.of(new ChimneyRabble()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    void tokenCannotAttackOnTheTurnItEnters() {
        harness.setHand(player1, List.of(new ChimneyRabble()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        int tokenIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Phyrexian Goblin"));
        assertThatThrownBy(() -> declareAttackers(List.of(tokenIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void entryTriggerCreatesTokenEvenAfterSourceDies() {
        harness.setHand(player1, List.of(new ChimneyRabble()));
        harness.setHand(player2, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Chimney Rabble"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Chimney Rabble");
        harness.assertInGraveyard(player1, "Chimney Rabble");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(1);
        assertThat(countPermanents(player2, "Phyrexian Goblin")).isZero();
    }
}
