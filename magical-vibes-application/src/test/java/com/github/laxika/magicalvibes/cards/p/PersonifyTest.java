package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonifyTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a creature you control and creates a colorless Shapeshifter token")
    void flickersCreatureAndCreatesToken() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Personify()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bearsId);
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Shapeshifter")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == null
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SHAPESHIFTER)
                        && permanent.getCard().getKeywords().contains(Keyword.CHANGELING));
    }

    @Test
    @DisplayName("Returns a stolen creature under its owner's control")
    void returnsUnderOwnersControl() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Personify()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature you do not control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Personify()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
