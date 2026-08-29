package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.FoulFamiliar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DoomsdaySpecterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return a blue or black creature you control")
    void etbPromptsForBlueOrBlackCreature() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new CloudSprite()).getId();
        UUID blackId = harness.addToBattlefieldAndReturn(player1, new FoulFamiliar()).getId();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new CloudSprite());

        castAndResolveSpell();

        UUID specterId = harness.getPermanentId(player1, "Doomsday Specter");
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(blueId, blackId, specterId);
    }

    @Test
    @DisplayName("Choosing a matching creature returns it to its owner's hand")
    void chosenCreatureReturnsToHand() {
        UUID familiarId = harness.addToBattlefieldAndReturn(player1, new FoulFamiliar()).getId();

        castAndResolveSpell();
        harness.handlePermanentChosen(player1, familiarId);

        harness.assertInHand(player1, "Foul Familiar");
        harness.assertOnBattlefield(player1, "Doomsday Specter");
    }

    @Test
    @DisplayName("Combat damage lets the controller choose a card for the damaged player to discard")
    void combatDamagePromptsControllerChoiceAndDiscardsChosenCard() {
        addAttackingSpecter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new LightningBolt(), new Island())));

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.targetPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.discardMode()).isTrue();
        assertThat(choice.exileMode()).isFalse();
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertInHand(player2, "Island");
    }

    @Test
    @DisplayName("Combat damage does not prompt when the damaged player's hand is empty")
    void combatDamageWithEmptyHandDoesNotPrompt() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolveSpell() {
        harness.setHand(player1, List.of(new DoomsdaySpecter()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addAttackingSpecter(Player player) {
        Permanent specter = new Permanent(new DoomsdaySpecter());
        specter.setSummoningSick(false);
        specter.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(specter);
        return specter;
    }
}
