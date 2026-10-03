package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PravaOfTheSteelLegion.class, GrizzlyBears.class})
class PravaOfTheSteelLegionTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, Prava buffs your creature tokens only")
    void buffsOwnCreatureTokensDuringYourTurn() {
        addCreatureReady(player1, new PravaOfTheSteelLegion());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, tokenCreature("Soldier Token"));
        addCreatureReady(player2, tokenCreature("Zombie Token"));

        Permanent ownToken = findPermanent(player1, "Soldier Token");
        Permanent ownNontoken = findPermanent(player1, "Grizzly Bears");
        Permanent opponentToken = findPermanent(player2, "Zombie Token");

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownNontoken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNontoken)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prava creates a Soldier token when its ability resolves")
    void createsSoldierToken() {
        addCreatureReady(player1, new PravaOfTheSteelLegion());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(soldier.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(5);
    }

    private Card tokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
