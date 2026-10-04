package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.d.DeftDuelist;
import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamingGambit.class, ElspethKnightErrant.class, CylianElf.class, DeftDuelist.class})
class FlamingGambitTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted player may redirect the damage to a creature they control")
    void targetedPlayerMayRedirectDamageToCreature() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosen.getId(), other.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(chosen.getMarkedDamage()).isEqualTo(1);
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the redirection deals the damage to the targeted player")
    void decliningRedirectDealsDamageToPlayer() {
        harness.addToBattlefield(player2, new CylianElf());
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A planeswalker's controller makes the redirection choice")
    void planeswalkerControllerMayRedirectDamage() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        int loyaltyBefore = planeswalker.getCounterCount(CounterType.LOYALTY);
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, planeswalker.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Without a creature, Flaming Gambit deals damage to the target without a may choice")
    void noCreatureMeansNoRedirectionChoice() {
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flaming Gambit may target its caster as a player")
    void mayTargetItsController() {
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Redirection offers only creatures controlled by the targeted player")
    void redirectionOffersOnlyCreaturesControlledByTargetPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("A creature leaving before resolution removes the redirection choice")
    void creatureLeavingBeforeResolutionRemovesRedirectionChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback deals damage and exiles Flaming Gambit")
    void flashbackDealsDamageAndExilesCard() {
        harness.setGraveyard(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertNotInGraveyard(player1, "Flaming Gambit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flaming Gambit"));
    }

    @Test
    @DisplayName("Flaming Gambit cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining redirection damages the targeted planeswalker rather than its controller")
    void decliningRedirectDamagesPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, 2, planeswalker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback preserves X when damage is redirected and exiles the spell")
    void flashbackCanRedirectLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setGraveyard(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(creature.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Cylian Elf");
        harness.assertInGraveyard(player2, "Cylian Elf");
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player1, "Flaming Gambit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flaming Gambit"));
    }

    @Test
    @DisplayName("The chosen creature is not targeted and may have shroud")
    void mayRedirectDamageToCreatureWithShroud() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DeftDuelist());
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(creature.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Deft Duelist");
        harness.assertInGraveyard(player2, "Deft Duelist");
    }

    @Test
    @DisplayName("X may be zero and the spell resolves without changing life")
    void zeroDamageResolves() {
        harness.setHand(player1, List.of(new FlamingGambit()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Flaming Gambit");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
