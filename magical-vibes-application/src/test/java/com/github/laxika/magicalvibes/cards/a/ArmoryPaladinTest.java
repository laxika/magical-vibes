package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoryPaladin.class, GiantGrowth.class, GrizzlyBears.class, LeoninScimitar.class, Rancor.class,
        Plains.class, TimeWarp.class})
class ArmoryPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card when you cast an Equipment spell")
    void exilesTopCardForEquipmentSpell() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exiles the top card when you cast an Aura spell")
    void exilesTopCardForAuraSpell() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Rancor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Aura, non-Equipment spell")
    void doesNotTriggerForOtherSpells() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The exiled spell can be cast by paying its normal mana cost")
    void castsExiledSpell() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("An opponent's Equipment spell does not trigger the ability")
    void opponentEquipmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LeoninScimitar()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Casting Equipment with an empty library still resolves the spell")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Play permission lasts through the next turn and then expires without moving the card")
    void permissionExpiresAfterNextTurn() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("The exiled card can be played as a land")
    void playsExiledLand() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("Permission expires at the end of your next turn even when it is an extra turn")
    void permissionExpiresAfterExtraTurn() {
        harness.addToBattlefield(player1, new ArmoryPaladin());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LeoninScimitar(), new TimeWarp()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }
}
