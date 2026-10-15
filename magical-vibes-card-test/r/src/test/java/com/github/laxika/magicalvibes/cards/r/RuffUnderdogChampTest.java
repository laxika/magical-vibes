package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuffUnderdogChamp.class, GrizzlyBears.class})
class RuffUnderdogChampTest extends BaseCardTest {

    private static Card creature(String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    @Test
    @DisplayName("Makes Hounds Dogs and buffs own Dogs after a match loss")
    void appliesDogTypeAndUnderdogBonus() {
        harness.addToBattlefield(player1, new RuffUnderdogChamp());
        Permanent hound = harness.addToBattlefieldAndReturn(player1, creature("Hound", 1, 1, CardSubtype.HOUND));
        Permanent dog = harness.addToBattlefieldAndReturn(player1, creature("Dog", 2, 2, CardSubtype.DOG));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingHound = harness.addToBattlefieldAndReturn(player2,
                creature("Opposing Hound", 1, 1, CardSubtype.HOUND));

        assertThat(gqs.effectiveCreatureSubtypes(gd, hound))
                .contains(CardSubtype.DOG)
                .doesNotContain(CardSubtype.HOUND);
        assertThat(gqs.getEffectivePower(gd, hound)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingHound)).isEqualTo(1);

        gd.playersWhoLostGameThisMatch.add(player1.getId());

        assertThat(gqs.getEffectivePower(gd, hound)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hound)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingHound)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not apply Underdog before the controller loses a match game")
    void doesNotApplyUnderdogWithoutMatchLoss() {
        harness.addToBattlefield(player1, new RuffUnderdogChamp());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, creature("Hound", 1, 1, CardSubtype.HOUND));

        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(1);
    }

    @Test
    @DisplayName("Hounds remain Dogs after a revealed Ruff leaves the battlefield")
    void dogErrataPersistsAfterRuffLeaves() {
        Permanent ruff = harness.addToBattlefieldAndReturn(player1, new RuffUnderdogChamp());
        Permanent hound = harness.addToBattlefieldAndReturn(player2,
                creature("Legacy Hound", 1, 1, CardSubtype.HOUND));

        assertThat(gqs.effectiveCreatureSubtypes(gd, hound)).contains(CardSubtype.DOG);
        gd.playerBattlefields.get(player1.getId()).remove(ruff);
        gd.playerGraveyards.get(player1.getId()).add(ruff.getCard());

        assertThat(gqs.effectiveCreatureSubtypes(gd, hound))
                .contains(CardSubtype.DOG)
                .doesNotContain(CardSubtype.HOUND);
    }

    @Test
    @DisplayName("An opponent's prior loss does not enable the controller's Underdog")
    void opponentLossDoesNotEnableUnderdog() {
        Permanent ruff = harness.addToBattlefieldAndReturn(player1, new RuffUnderdogChamp());
        gd.playersWhoLostGameThisMatch.add(player2.getId());

        assertThat(gqs.getEffectivePower(gd, ruff)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruff)).isEqualTo(2);
    }

    @Test
    @DisplayName("Underdog boosts Ruff itself even when Ruff is no longer a Dog")
    void underdogBoostsRuffRegardlessOfItsCreatureType() {
        Permanent ruff = harness.addToBattlefieldAndReturn(player1, new RuffUnderdogChamp());
        ruff.setTransientCreatureTypeOverride(CardSubtype.BEAR);
        gd.playersWhoLostGameThisMatch.add(player1.getId());

        assertThat(gqs.effectiveCreatureSubtypes(gd, ruff)).doesNotContain(CardSubtype.DOG);
        assertThat(gqs.getEffectivePower(gd, ruff)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ruff)).isEqualTo(3);
    }

}
