package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenerousGift.class, GrizzlyBears.class, Forest.class})
class GenerousGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target permanent and gives its controller a 3/3 green Elephant")
    void destroysPermanentAndCreatesElephantForController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castGenerousGift(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elephant")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == CardColor.GREEN
                        && permanent.getCard().getPower() == 3
                        && permanent.getCard().getToughness() == 3
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ELEPHANT));
    }

    @Test
    @DisplayName("Can destroy a land and give its controller an Elephant")
    void destroysNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        castGenerousGift(harness.getPermanentId(player2, "Forest"));

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elephant"));
    }

    @Test
    @DisplayName("Cannot target a card that is not a permanent")
    void requiresPermanentTarget() {
        harness.setHand(player1, List.of(new GenerousGift()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGenerousGift(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new GenerousGift()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
