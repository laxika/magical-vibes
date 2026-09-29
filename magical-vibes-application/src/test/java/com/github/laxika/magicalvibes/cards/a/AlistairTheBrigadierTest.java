package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlistairTheBrigadier.class, Spellbook.class, GrizzlyBears.class})
class AlistairTheBrigadierTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a historic spell creates a 1/1 white Soldier token")
    void historicSpellCreatesSoldier() {
        harness.addToBattlefield(player1, new AlistairTheBrigadier());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-historic spell does not create a Soldier token")
    void nonHistoricSpellDoesNotCreateSoldier() {
        harness.addToBattlefield(player1, new AlistairTheBrigadier());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Paying {8} on attack boosts creatures by the historic permanent count")
    void payingOnAttackBoostsByHistoricCount() {
        addCreatureReady(player1, new AlistairTheBrigadier());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent alistair = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gqs.getEffectivePower(gd, alistair)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, alistair)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the attack may-pay leaves creatures unboosted")
    void decliningOnAttackDoesNotBoost() {
        addCreatureReady(player1, new AlistairTheBrigadier());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        Permanent alistair = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(gqs.getEffectivePower(gd, alistair)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, alistair)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
