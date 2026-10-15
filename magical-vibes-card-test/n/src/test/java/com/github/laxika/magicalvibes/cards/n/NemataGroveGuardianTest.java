package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MagnigothTreefolk;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({NemataGroveGuardian.class, MagnigothTreefolk.class, Xenograft.class})
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
        Permanent nonSaproling = addCreatureReady(player1, new MagnigothTreefolk());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivingOwnSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivingOwnSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSaproling)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrificed.getId()));
    }

    @Test
    @DisplayName("Saproling boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new NemataGroveGuardian());
        Permanent sacrificed = addCreatureReady(player1, createSaprolingToken());
        Permanent saproling = addCreatureReady(player1, createSaprolingToken());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(2);

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
        addCreatureReady(player1, new MagnigothTreefolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");
    }

    @Test
    @DisplayName("An opponent's Saproling cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsSaproling() {
        addCreatureReady(player1, new NemataGroveGuardian());
        addCreatureReady(player2, createSaprolingToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");
    }

    @Test
    @DisplayName("Saprolings created after the boost resolves are not boosted")
    void laterSaprolingsAreNotBoosted() {
        addCreatureReady(player1, new NemataGroveGuardian());
        Permanent sacrificed = addCreatureReady(player1, createSaprolingToken());
        Permanent survivor = addCreatureReady(player1, createSaprolingToken());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent laterToken = findPermanents(player1, "Saproling").stream()
                .filter(permanent -> !permanent.getId().equals(survivor.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterToken)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated sacrifices give cumulative boosts")
    void repeatedBoostsAccumulate() {
        addCreatureReady(player1, new NemataGroveGuardian());
        Permanent firstSacrifice = addCreatureReady(player1, createSaprolingToken());
        Permanent secondSacrifice = addCreatureReady(player1, createSaprolingToken());
        Permanent survivor = addCreatureReady(player2, createSaprolingToken());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstSacrifice.getId()));
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondSacrifice.getId()));
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Can sacrifice itself if it is also a Saproling")
    void canSacrificeSourceIfItIsASaproling() {
        Permanent xenograft = harness.addToBattlefieldAndReturn(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.SAPROLING);
        Permanent source = addCreatureReady(player1, new NemataGroveGuardian());

        assertThat(gqs.effectiveCreatureSubtypes(gd, source)).contains(CardSubtype.SAPROLING);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(source.getId()));
    }

    @Test
    @DisplayName("The boost still resolves after sacrificing Nemata itself")
    void boostResolvesAfterSourceIsSacrificed() {
        Permanent xenograft = harness.addToBattlefieldAndReturn(player1, new Xenograft());
        xenograft.setChosenSubtype(CardSubtype.SAPROLING);
        addCreatureReady(player1, new NemataGroveGuardian());
        Permanent opposingSaproling = addCreatureReady(player2, createSaprolingToken());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Nemata, Grove Guardian");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opposingSaproling)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSaproling)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Saproling created in response receives the pending boost")
    void saprolingCreatedInResponseIsBoosted() {
        addCreatureReady(player1, new NemataGroveGuardian());
        addCreatureReady(player1, createSaprolingToken());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Saproling");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
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
