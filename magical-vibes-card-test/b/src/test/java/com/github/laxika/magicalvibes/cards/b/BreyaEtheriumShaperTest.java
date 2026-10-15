package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ColossusOfSardia;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreyaEtheriumShaper.class, GrizzlyBears.class, ColossusOfSardia.class, KarnLiberated.class})
class BreyaEtheriumShaperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two blue 1/1 Thopter artifact creature tokens with flying")
    void etbCreatesTwoThopters() {
        addBreya();

        assertThat(findPermanents(player1, "Thopter")).hasSize(2);
        assertThat(findPermanents(player1, "Thopter")).allSatisfy(thopter -> {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(thopter.getCard().getColors()).containsExactly(CardColor.BLUE);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        });
    }

    @Test
    @DisplayName("The damage mode sacrifices two artifacts and deals 3 damage to a player")
    void damageModeSacrificesArtifactsAndDamagesPlayer() {
        addBreya();
        Permanent firstThopter = findPermanents(player1, "Thopter").get(0);
        Permanent secondThopter = findPermanents(player1, "Thopter").get(1);
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.handlePermanentChosen(player1, firstThopter.getId());
        harness.handlePermanentChosen(player1, secondThopter.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("The debuff mode gives a target creature -4/-4 until end of turn")
    void debuffModeKillsTargetCreature() {
        addBreya();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.handlePermanentChosen(player1, thopters.get(0).getId());
        harness.handlePermanentChosen(player1, thopters.get(1).getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The life-gain mode sacrifices two artifacts and gains 5 life")
    void lifeGainModeGainsFiveLife() {
        addBreya();
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.handlePermanentChosen(player1, thopters.get(0).getId());
        harness.handlePermanentChosen(player1, thopters.get(1).getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    void damageModeRejectsCreatureTarget() {
        addBreya();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeRemovesThreeLoyaltyFromPlaneswalker() {
        addBreya();
        Permanent karn = harness.enterBattlefieldAndReturn(player2, new KarnLiberated());
        int loyaltyBefore = karn.getCounterCount(CounterType.LOYALTY);
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, karn.getId());
        harness.handlePermanentChosen(player1, thopters.get(0).getId());
        harness.handlePermanentChosen(player1, thopters.get(1).getId());
        harness.passBothPriorities();

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 3);
        harness.assertOnBattlefield(player2, "Karn Liberated");
    }

    @Test
    void damageModeResolvesWhenBreyaIsSacrificedAsFirstArtifact() {
        Permanent breya = addBreya();
        Permanent thopter = findPermanents(player1, "Thopter").get(0);
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.handlePermanentChosen(player1, breya.getId());
        harness.handlePermanentChosen(player1, thopter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Breya, Etherium Shaper");
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void debuffExpiresAtEndOfTurn() {
        addBreya();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossusOfSardia());
        int powerBefore = gqs.getEffectivePower(gd, target);
        int toughnessBefore = gqs.getEffectiveToughness(gd, target);
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.handlePermanentChosen(player1, thopters.get(0).getId());
        harness.handlePermanentChosen(player1, thopters.get(1).getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBefore - 4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughnessBefore - 4);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughnessBefore);
    }

    @Test
    void cannotActivateWithOnlyOneArtifact() {
        harness.addToBattlefield(player1, new BreyaEtheriumShaper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Breya, Etherium Shaper");
    }

    @Test
    void cannotSacrificeOpponentsArtifact() {
        addBreya();
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new ColossusOfSardia());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Colossus of Sardia");
    }

    private Permanent addBreya() {
        Permanent breya = harness.enterBattlefieldAndReturn(player1, new BreyaEtheriumShaper());
        harness.passBothPriorities();
        return breya;
    }
}
