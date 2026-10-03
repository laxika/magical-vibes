package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FanaticalFirebrand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.t.TalasScout;
import com.github.laxika.magicalvibes.cards.m.MatterReshaper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreechesBrazenPlunderer.class, TalasScout.class, GrizzlyBears.class,
        HermeticStudy.class, Divination.class, Forest.class, FanaticalFirebrand.class,
        MatterReshaper.class, Humble.class})
class BreechesBrazenPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("A Pirate dealing combat damage exiles the opponent's top card and grants play permission")
    void pirateCombatDamageExilesTopCard() {
        Card topCard = new Divination();
        Card nextCard = new Forest();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("The same trigger exiles only one card when multiple Pirates damage one opponent")
    void multiplePiratesAreBatched() {
        Card first = new Divination();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(first, second));
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Noncombat damage from a Pirate also grants the controller an any-color cast")
    void pirateNoncombatDamageGrantsAnyColorCast() {
        Permanent breeches = addCreatureReady(player1, new BreechesBrazenPlunderer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(breeches.getId());
        Divination topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Damage from a non-Pirate does not trigger Breeches")
    void nonPirateDamageDoesNotTrigger() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(bears.getId());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("A sacrificed Pirate still triggers Breeches when its ability deals damage")
    void sacrificedPirateDamageTriggersBreeches() {
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new FanaticalFirebrand());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.assertInGraveyard(player1, "Fanatical Firebrand");
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("An exiled land may be played but still uses the normal land allowance")
    void exiledLandUsesNormalLandAllowance() {
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        harness.assertOnBattlefield(player1, "Forest");
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unplayed cards stay exiled but lose permission after the turn")
    void unplayedCardPermissionExpires() {
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Pirate damaging its own controller does not trigger Breeches")
    void damageToOwnControllerDoesNotTrigger() {
        Permanent breeches = addCreatureReady(player1, new BreechesBrazenPlunderer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(breeches.getId());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty opponent library exiles nothing and grants no play permission")
    void emptyLibraryExilesNothing() {
        harness.setLibrary(player2, List.of());
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Any-color permission cannot replace an explicit colorless mana requirement")
    void anyColorPermissionStillRequiresColorlessMana() {
        Card topCard = new MatterReshaper();
        harness.setLibrary(player2, List.of(topCard));
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("Breeches cannot trigger after losing its abilities")
    void losingAbilitiesPreventsPirateDamageTrigger() {
        Permanent breeches = addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, breeches.getId());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Breeches triggers when it dies in the same combat damage event as another Pirate hits")
    void simultaneousCombatDeathDoesNotPreventTrigger() {
        addCreatureReady(player1, new BreechesBrazenPlunderer());
        addCreatureReady(player1, new TalasScout());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 1));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Breeches, Brazen Plunderer");
        harness.assertLife(player2, 19);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }
}
