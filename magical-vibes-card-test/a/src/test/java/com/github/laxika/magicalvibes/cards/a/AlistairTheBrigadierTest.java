package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlistairTheBrigadier.class, Spellbook.class, GrizzlyBears.class, AnUnearthlyChild.class})
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

    @Test
    @DisplayName("A nonartifact legendary spell creates a Soldier before the spell resolves")
    void legendarySpellCreatesSoldierBeforeResolving() {
        harness.addToBattlefield(player1, new AlistairTheBrigadier());
        harness.setHand(player1, List.of(new AlistairTheBrigadier()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player1, "Alistair, the Brigadier")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting Alistair without another Alistair does not trigger itself")
    void castingAlistairDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new AlistairTheBrigadier()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Alistair, the Brigadier");
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("A nonlegendary Saga spell creates a Soldier before the Saga resolves")
    void sagaSpellCreatesSoldier() {
        harness.addToBattlefield(player1, new AlistairTheBrigadier());
        harness.setHand(player1, List.of(new AnUnearthlyChild()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "An Unearthly Child");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent casting a historic spell does not create a Soldier")
    void opponentsHistoricSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AlistairTheBrigadier());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Attack boost counts Sagas and ignores opposing historic permanents")
    void attackCountsOnlyControllersHistoricPermanents() {
        Permanent alistair = addCreatureReady(player1, new AlistairTheBrigadier());
        harness.addToBattlefield(player1, new AnUnearthlyChild());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingAlistair = addCreatureReady(player2, new AlistairTheBrigadier());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, alistair)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, alistair)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingAlistair)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingAlistair)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attack boost counts at resolution and remains fixed for existing creatures until cleanup")
    void boostUsesResolutionCountAndExpires() {
        Permanent alistair = addCreatureReady(player1, new AlistairTheBrigadier());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        declareAttackers(List.of(0));
        harness.addToBattlefield(player1, new Spellbook());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, alistair)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, alistair)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.addToBattlefield(player1, new Spellbook());
        Permanent lateBears = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, alistair)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, lateBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateBears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, alistair)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, alistair)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Seven mana cannot pay for the attack boost")
    void insufficientManaDoesNotBoost() {
        Permanent alistair = addCreatureReady(player1, new AlistairTheBrigadier());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, alistair)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, alistair)).isEqualTo(3);
    }
}
