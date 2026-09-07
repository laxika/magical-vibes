package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HareRaising;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PollenShieldHare.class, HareRaising.class, GrizzlyBears.class})
class PollenShieldHareTest extends BaseCardTest {

    @Test
    void creatureTokensYouControlGetPlusOnePlusOne() {
        harness.addToBattlefield(player1, new PollenShieldHare());
        harness.addToBattlefield(player1, createTokenCreature("Rabbit Token", 1, 1));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, createTokenCreature("Opponent Token", 1, 1));

        Permanent ownToken = findPermanent(player1, "Rabbit Token");
        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opponentToken = findPermanent(player2, "Opponent Token");

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(1);
    }

    @Test
    void adventureBoostsTargetByCreatureCountAndGrantsVigilanceUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        PollenShieldHare card = new PollenShieldHare();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void adventureCanOnlyTargetCreatureYouControl() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PollenShieldHare card = new PollenShieldHare();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setToken(true);
        return card;
    }
}
