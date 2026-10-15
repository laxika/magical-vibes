package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CripplingFatigue;
import com.github.laxika.magicalvibes.cards.c.CabalTorturer;
import com.github.laxika.magicalvibes.cards.t.TaintedField;
import com.github.laxika.magicalvibes.cards.t.TerohsFaithful;
import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrengthOfIsolation.class, TerohsFaithful.class, TaintedField.class,
        Unhinge.class, CripplingFatigue.class, CabalTorturer.class})
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
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TaintedField());
        harness.setHand(player1, List.of(new StrengthOfIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

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

    @Test
    @DisplayName("Multiple copies stack their boosts only on the enchanted creature")
    void multipleAurasBoostOnlyTheirHost() {
        Permanent host = addCreatureReady(player1, new TerohsFaithful());
        Permanent other = addCreatureReady(player1, new TerohsFaithful());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StrengthOfIsolation());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new StrengthOfIsolation());
        first.setAttachedTo(host.getId());
        second.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(8);
        assertThat(gqs.hasProtectionFrom(gd, host, CardColor.BLACK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, other, CardColor.BLACK)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(6);
        assertThat(gqs.hasProtectionFrom(gd, host, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("Aura goes to the graveyard when its target leaves before resolution")
    void targetLeavingBeforeResolutionPreventsAttachment() {
        Permanent target = addCreatureReady(player1, new TerohsFaithful());
        StrengthOfIsolation isolation = new StrengthOfIsolation();
        harness.setHand(player1, List.of(isolation));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Strength of Isolation");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(isolation.getId()));
    }

    @Test
    @DisplayName("Madness without a legal creature target puts the Aura into the graveyard")
    void madnessWithoutCreatureTargetDoesNotSpendMana() {
        StrengthOfIsolation isolation = discardViaUnhinge();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.withAutoStop(gd.currentStep, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(isolation.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(isolation.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Strength of Isolation");
    }

    @Test
    @DisplayName("Protection from black prevents black creatures from blocking")
    void blackCreatureCannotBlockEnchantedAttacker() {
        Permanent host = addCreatureReady(player1, new TerohsFaithful());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StrengthOfIsolation());
        aura.setAttachedTo(host.getId());
        addCreatureReady(player2, new CabalTorturer());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature may block a black attacker and prevents its damage")
    void enchantedBlockerPreventsBlackCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new CabalTorturer());
        Permanent host = addCreatureReady(player2, new TerohsFaithful());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new StrengthOfIsolation());
        aura.setAttachedTo(host.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();

        assertThat(host.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(host);
    }

    private StrengthOfIsolation discardViaUnhinge() {
        StrengthOfIsolation isolation = new StrengthOfIsolation();
        harness.setHand(player1, List.of(isolation));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return isolation;
    }
}
