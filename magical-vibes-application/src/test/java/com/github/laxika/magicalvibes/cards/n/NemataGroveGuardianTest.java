package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NemataGroveGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{G} creates a 1/1 green Saproling token")
    void createsSaprolingToken() {
        addCreatureReady(player1, new NemataGroveGuardian());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("Sacrificing a Saproling boosts Saprolings on both sides")
    void boostsAllSaprolings() {
        addCreatureReady(player1, new NemataGroveGuardian());
        Permanent sacrificed = addCreatureReady(player1, createSaprolingToken());
        Permanent survivingOwnSaproling = addCreatureReady(player1, createSaprolingToken());
        Permanent opposingSaproling = addCreatureReady(player2, createSaprolingToken());
        Permanent nonSaproling = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivingOwnSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivingOwnSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSaproling)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrificed.getId()));
    }

    @Test
    @DisplayName("Saproling boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new NemataGroveGuardian());
        Permanent saproling = addCreatureReady(player1, createSaprolingToken());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Saproling creature")
    void cannotActivateWithoutSaproling() {
        addCreatureReady(player1, new NemataGroveGuardian());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");
    }

    private Card createSaprolingToken() {
        Card card = new Card();
        card.setName("Saproling");
        card.setType(CardType.CREATURE);
        card.setManaCost("{0}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        card.setToken(true);
        return card;
    }
}
