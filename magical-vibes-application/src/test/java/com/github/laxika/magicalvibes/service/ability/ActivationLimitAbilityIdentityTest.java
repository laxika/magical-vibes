package com.github.laxika.magicalvibes.service.ability;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseThisActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Activation limits ("Activate only once each turn") follow the ability, not its position in the
 * effective ability list: when an earlier ability is lost ("loses this ability"), later abilities
 * move up the list but keep their own activation counts. Exile-only abilities are not part of a
 * battlefield permanent's ability list, so ability indices match the list the client shows.
 */
class ActivationLimitAbilityIdentityTest extends BaseCardTest {

    private static Card createShifter() {
        Card card = new Card();
        card.setName("Test Shifter");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new GainLifeEffect(1), new LoseThisActivatedAbilityEffect()),
                "Gain 1 life, then this creature loses this ability."));
        card.addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new GainLifeEffect(2)),
                "Gain 2 life. Activate only once each turn.", 1));
        card.addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new GainLifeEffect(3)),
                "Gain 3 life. Activate only once each turn.", 1));
        return card;
    }

    private void activateAndResolve(int abilityIndex) {
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private int life() {
        return gd.playerLifeTotals.get(player1.getId());
    }

    @Test
    @DisplayName("A once-per-turn ability activated before an earlier ability is lost stays used after it moves up")
    void onceEachTurnAbilityStaysUsedAfterEarlierAbilityIsLost() {
        addCreatureReady(player1, createShifter());
        activateAndResolve(1);
        activateAndResolve(0);
        assertThat(life()).isEqualTo(23);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("A once-per-turn ability doesn't inherit the count of the ability that moved into its old position")
    void laterAbilityDoesNotInheritEarlierAbilityCount() {
        addCreatureReady(player1, createShifter());
        activateAndResolve(1);
        activateAndResolve(0);

        activateAndResolve(1);

        assertThat(life()).isEqualTo(26);
    }

    @Test
    @DisplayName("A once-per-turn ability activated before the loss is still limited at its new position")
    void lastAbilityStaysUsedAtItsNewPosition() {
        addCreatureReady(player1, createShifter());
        activateAndResolve(2);
        activateAndResolve(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        activateAndResolve(0);
        assertThat(life()).isEqualTo(26);
    }

    @Test
    @DisplayName("A once-per-turn ability first activated after the loss is limited to once")
    void onceEachTurnAbilityActivatedOnlyAfterLossIsLimited() {
        addCreatureReady(player1, createShifter());
        activateAndResolve(0);

        activateAndResolve(0);

        assertThat(life()).isEqualTo(23);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("An exile-only ability is left out of a battlefield permanent's ability list")
    void exileOnlyAbilityIsNotListedOnTheBattlefield() {
        Card card = new Card();
        card.setName("Test Returner");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new ReturnSourceCardFromExileToBattlefieldEffect(true, false)),
                "Return this card from exile to the battlefield tapped.").withExileOnly());
        card.addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new GainLifeEffect(2)), "Gain 2 life."));
        Permanent permanent = addCreatureReady(player1, card);

        assertThat(gs.getEffectiveActivatedAbilities(gd, permanent))
                .extracting(ActivatedAbility::getDescription)
                .containsExactly("Gain 2 life.");
        activateAndResolve(0);

        assertThat(life()).isEqualTo(22);
    }
}
