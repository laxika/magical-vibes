package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FranticPurification.class, GloriousAnthem.class, GrizzlyBears.class, RavensCrime.class})
class FranticPurificationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new FranticPurification()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FranticPurification()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Casting from madness destroys target enchantment")
    void castsFromMadness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        FranticPurification purification = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Frantic Purification");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(purification.getId()));
    }

    private FranticPurification discardViaRavensCrime() {
        FranticPurification purification = new FranticPurification();
        harness.setHand(player1, List.of(purification));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return purification;
    }
}
