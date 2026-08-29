package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArtisanOfKozilekTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Artisan returns a targeted creature card from the graveyard")
    void castReturnsTargetedCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        ArtisanOfKozilek artisan = new ArtisanOfKozilek();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(artisan));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Declining Artisan's cast trigger leaves the graveyard unchanged")
    void declineCastTriggerDoesNothing() {
        GrizzlyBears bears = new GrizzlyBears();
        ArtisanOfKozilek artisan = new ArtisanOfKozilek();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(artisan));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(artisan.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Artisan cannot target a noncreature card in the graveyard")
    void castDoesNotTargetNoncreature() {
        Spellbook spellbook = new Spellbook();
        ArtisanOfKozilek artisan = new ArtisanOfKozilek();
        harness.setGraveyard(player1, List.of(spellbook));
        harness.setHand(player1, List.of(artisan));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(artisan.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spellbook.getId()));
    }

    @Test
    @DisplayName("Attacking Artisan makes the defending player sacrifice two permanents")
    void attackForcesDefendingPlayerToSacrificeTwoPermanents() {
        Permanent artisan = addCreatureReady(player1, new ArtisanOfKozilek());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).contains(bears.getId(), mountain.getId(), spellbook.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId(), mountain.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spellbook);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artisan);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(mountain.getCard().getId()));
    }
}
