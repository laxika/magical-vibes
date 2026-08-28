package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamblingCieth.class, Spellbook.class, GrizzlyBears.class})
class ShamblingCiethTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ShamblingCieth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent cieth = findPermanent(player1, "Shambling Cie'th");
        assertThat(cieth.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A noncreature spell creates a may-pay trigger from the graveyard")
    void noncreatureSpellCreatesMayPayTrigger() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{B}");
    }

    @Test
    @DisplayName("Paying {B} returns it from the graveyard to hand")
    void payingReturnsToHand() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(cieth.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(cieth.getId()));
    }

    @Test
    @DisplayName("Declining the payment keeps it in the graveyard")
    void decliningKeepsItInGraveyard() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(cieth.getId()));
    }

    @Test
    @DisplayName("A creature spell does not create the graveyard trigger")
    void creatureSpellDoesNotTrigger() {
        ShamblingCieth cieth = new ShamblingCieth();
        harness.setGraveyard(player1, List.of(cieth));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(cieth.getId()));
    }
}
