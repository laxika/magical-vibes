package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeoninOfTheLostPride;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({AlseidOfLifesBounty.class, LeoninOfTheLostPride.class, OmenOfTheSea.class, Forest.class})
class AlseidOfLifesBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing this creature grants chosen-color protection to a creature you control")
    void sacrificeGrantsChosenColorProtectionToCreature() {
        addCreatureReady(player1, new AlseidOfLifesBounty());
        Permanent target = addCreatureReady(player1, new LeoninOfTheLostPride());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        harness.assertInGraveyard(player1, "Alseid of Life's Bounty");
    }

    @Test
    @DisplayName("The ability can target an enchantment you control")
    void canTargetControlledEnchantment() {
        addCreatureReady(player1, new AlseidOfLifesBounty());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OmenOfTheSea());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target an opponent's permanent or a land")
    void cannotTargetOpponentPermanentOrLand() {
        addCreatureReady(player1, new AlseidOfLifesBounty());
        Permanent opponentCreature = addCreatureReady(player2, new LeoninOfTheLostPride());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new Forest());
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                null,
                harness.getPermanentId(player1, "Forest")
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation requires one mana and cannot sacrifice Alseid without paying it")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new AlseidOfLifesBounty());
        Permanent target = addCreatureReady(player1, new LeoninOfTheLostPride());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Alseid of Life's Bounty");
        harness.assertNotInGraveyard(player1, "Alseid of Life's Bounty");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One mana of any color pays the cost even when Alseid is summoning sick")
    void paysGenericManaAndSacrificesImmediately() {
        harness.addToBattlefield(player1, new AlseidOfLifesBounty());
        Permanent target = addCreatureReady(player1, new LeoninOfTheLostPride());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Alseid of Life's Bounty");
        harness.assertInGraveyard(player1, "Alseid of Life's Bounty");
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();

        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Chosen protection expires at the end of the turn")
    void protectionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new AlseidOfLifesBounty());
        Permanent target = addCreatureReady(player1, new LeoninOfTheLostPride());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLACK.name());

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();
        declareAttackers(List.of());
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Alseid may target itself but the ability has no legal target after sacrifice")
    void canTargetSelfAndAbilityFizzles() {
        Permanent alseid = addCreatureReady(player1, new AlseidOfLifesBounty());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, alseid.getId());
        harness.assertInGraveyard(player1, "Alseid of Life's Bounty");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Combat damage from Alseid gains its controller life")
    void lifelinkGainsLifeFromCombatDamage() {
        addCreatureReady(player1, new AlseidOfLifesBounty());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
