package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FreyaliseLlanowarsFury.class)
class FreyaliseLlanowarsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("+2 creates an Elf Druid token with a green mana ability")
    void plusTwoCreatesManaElfToken() {
        addReadyFreyalise(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elf Druid");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);

        token.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 destroys an artifact or enchantment")
    void minusTwoDestroysArtifactOrEnchantment() {
        addReadyFreyalise(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                permanent("Target Artifact", CardType.ARTIFACT, null));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Target Artifact");
        harness.assertInGraveyard(player2, "Target Artifact");
    }

    @Test
    @DisplayName("-2 cannot target a creature")
    void minusTwoRejectsCreatureTarget() {
        addReadyFreyalise(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                permanent("Target Creature", CardType.CREATURE, CardColor.GREEN));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("-6 draws a card for each green creature controlled")
    void minusSixDrawsForEachGreenCreature() {
        addReadyFreyalise(6);
        harness.addToBattlefield(player1, permanent("Green Creature One", CardType.CREATURE, CardColor.GREEN));
        harness.addToBattlefield(player1, permanent("Green Creature Two", CardType.CREATURE, CardColor.GREEN));
        harness.addToBattlefield(player1, permanent("Blue Creature", CardType.CREATURE, CardColor.BLUE));
        harness.setLibrary(player1, List.of(new Card(), new Card()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    private Permanent addReadyFreyalise(int loyalty) {
        Permanent freyalise = new Permanent(new FreyaliseLlanowarsFury());
        freyalise.setCounterCount(CounterType.LOYALTY, loyalty);
        freyalise.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(freyalise);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return freyalise;
    }

    private Card permanent(String name, CardType type, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setColor(color);
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        return card;
    }
}
