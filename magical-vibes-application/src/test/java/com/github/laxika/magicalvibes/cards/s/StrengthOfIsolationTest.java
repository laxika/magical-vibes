package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CripplingFatigue;
import com.github.laxika.magicalvibes.cards.t.TaintedField;
import com.github.laxika.magicalvibes.cards.t.TerohsFaithful;
import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.CardColor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrengthOfIsolation.class, TerohsFaithful.class, TaintedField.class,
        Unhinge.class, CripplingFatigue.class})
class StrengthOfIsolationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+2 and protection from black")
    void enchantedCreatureGetsBoostAndProtection() {
        Permanent faithful = addCreatureReady(player1, new TerohsFaithful());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfIsolation());
        aura.setAttachedTo(faithful.getId());

        assertThat(gqs.getEffectivePower(gd, faithful)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, faithful)).isEqualTo(6);
        assertThat(gqs.hasProtectionFrom(gd, faithful, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, faithful, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Removing Strength of Isolation removes its bonuses")
    void effectsStopWhenRemoved() {
        Permanent faithful = addCreatureReady(player1, new TerohsFaithful());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfIsolation());
        aura.setAttachedTo(faithful.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, faithful)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, faithful)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, faithful, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Resolving Strength of Isolation attaches it to a creature")
    void resolvesAndAttaches() {
        Permanent faithful = addCreatureReady(player1, new TerohsFaithful());
        harness.setHand(player1, List.of(new StrengthOfIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, faithful.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getClass() == StrengthOfIsolation.class
                        && faithful.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new TaintedField());
        harness.setHand(player1, List.of(new StrengthOfIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent land = findPermanent(player1, "Tainted Field");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Madness casts Strength of Isolation for white mana")
    void madnessCastsForWhiteMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TerohsFaithful());
        StrengthOfIsolation isolation = discardViaUnhinge();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(isolation.getId())
                        && target.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining madness puts Strength of Isolation into the graveyard")
    void decliningMadnessPutsItIntoGraveyard() {
        StrengthOfIsolation isolation = discardViaUnhinge();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(isolation.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(isolation.getId()));
    }

    @Test
    @DisplayName("Protection from black prevents black spells from targeting the enchanted creature")
    void protectionFromBlackPreventsBlackSpellTargeting() {
        Permanent faithful = addCreatureReady(player1, new TerohsFaithful());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfIsolation());
        aura.setAttachedTo(faithful.getId());

        harness.setHand(player2, List.of(new CripplingFatigue()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, faithful.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    private StrengthOfIsolation discardViaUnhinge() {
        StrengthOfIsolation isolation = new StrengthOfIsolation();
        harness.setHand(player1, List.of(isolation));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return isolation;
    }
}
