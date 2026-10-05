package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Personify.class, GrizzlyBears.class, LayClaim.class})
class PersonifyTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a creature you control and creates a colorless Shapeshifter token")
    void flickersCreatureAndCreatesToken() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Personify()));
        harness.addMana(player1, ManaColor.WHITE, 2);

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
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

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
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Personify()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates no token when the targeted creature has already been flickered")
    void createsNoTokenWhenTargetIsIllegal() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Personify(), new Personify()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, bearsId);
        harness.castInstant(player1, 0, bearsId);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isNotEqualTo(bearsId);
        assertThat(countPermanents(player1, "Shapeshifter")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Exiling a token does not return it but still creates a new Shapeshifter")
    void tokenTargetDoesNotReturn() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Personify()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        UUID tokenId = findPermanent(player1, "Shapeshifter").getId();
        harness.setHand(player1, List.of(new Personify()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, tokenId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Shapeshifter")).isEqualTo(1);
        assertThat(findPermanent(player1, "Shapeshifter").getId()).isNotEqualTo(tokenId);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
