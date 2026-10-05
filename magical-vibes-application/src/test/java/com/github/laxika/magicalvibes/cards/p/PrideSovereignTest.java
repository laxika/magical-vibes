package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RampagingHippo;
import com.github.laxika.magicalvibes.cards.a.AdornedPouncer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrideSovereign.class, AdornedPouncer.class, RampagingHippo.class})
class PrideSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/2 with no other Cats")
    void baseStatsWithNoOtherCats() {
        harness.addToBattlefield(player1, new PrideSovereign());
        Permanent sovereign = findPermanent(player1, "Pride Sovereign");

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 for each other Cat you control")
    void boostsForOtherCats() {
        harness.addToBattlefield(player1, new PrideSovereign());
        harness.addToBattlefield(player1, new AdornedPouncer());
        harness.addToBattlefield(player1, new AdornedPouncer());
        Permanent sovereign = findPermanent(player1, "Pride Sovereign");

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count itself, non-Cats, or opponent Cats")
    void doesNotCountSelfNonCatsOrOpponentCats() {
        harness.addToBattlefield(player1, new PrideSovereign());
        harness.addToBattlefield(player1, new RampagingHippo());
        harness.addToBattlefield(player2, new AdornedPouncer());
        Permanent sovereign = findPermanent(player1, "Pride Sovereign");

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability creates two 1/1 white Cat tokens with lifelink and exerts")
    void abilityCreatesCatTokensAndExerts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent sovereign = addCreatureReady(player1, new PrideSovereign());

        harness.addMana(player1, ManaColor.WHITE, 1);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        harness.activateAbility(player1, idx, 0, null, null);
        assertThat(sovereign.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(sovereign.getSkipUntapCount()).isGreaterThan(0);

        var cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(2);
        assertThat(cats).allSatisfy(cat -> {
            assertThat(cat.getCard().getPower()).isEqualTo(1);
            assertThat(cat.getCard().getToughness()).isEqualTo(1);
            assertThat(cat.getCard().getSubtypes()).contains(CardSubtype.CAT);
            assertThat(cat.getCard().getKeywords()).contains(Keyword.LIFELINK);
            assertThat(cat.getCard().getColor()).isEqualTo(CardColor.WHITE);
        });
    }

    @Test
    @DisplayName("Created Cat tokens contribute to the static boost")
    void tokensContributeToBoost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent sovereign = addCreatureReady(player1, new PrideSovereign());

        harness.addMana(player1, ManaColor.WHITE, 1);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(sovereign);
        harness.activateAbility(player1, idx, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exert is paid immediately before the token ability resolves")
    void exertIsPaidOnActivation() {
        Permanent sovereign = addCreatureReady(player1, new PrideSovereign());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sovereign.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Cat")).isZero();
        harness.performUntapStep(player1);
        assertThat(sovereign.isTapped()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exert skips one untap step and then expires")
    void exertSkipsOnlyNextUntap() {
        Permanent sovereign = addCreatureReady(player1, new PrideSovereign());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(sovereign.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(sovereign.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Changing controllers does not prevent untapping in the new controller's turn")
    void exertRemainsTiedToActivatingPlayer() {
        Permanent sovereign = addCreatureReady(player1, new PrideSovereign());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(sovereign);
        gd.playerBattlefields.get(player2.getId()).add(sovereign);
        harness.performUntapStep(player2);

        assertThat(sovereign.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(2);
        assertThat(countPermanents(player2, "Cat")).isZero();
        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Cat-count boost decreases when another Cat leaves")
    void boostUpdatesWhenCatLeaves() {
        harness.addToBattlefield(player1, new PrideSovereign());
        harness.addToBattlefield(player1, new AdornedPouncer());
        Permanent sovereign = findPermanent(player1, "Pride Sovereign");
        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Adorned Pouncer"));

        assertThat(gqs.getEffectivePower(gd, sovereign)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sovereign)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability cannot be activated with summoning sickness")
    void abilityCannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new PrideSovereign());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
