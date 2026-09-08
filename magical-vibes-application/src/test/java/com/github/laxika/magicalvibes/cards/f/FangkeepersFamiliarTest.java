package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SealOfStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FangkeepersFamiliar.class, FaerieInvaders.class, GrizzlyBears.class, SealOfStrength.class})
class FangkeepersFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode gains 3 life and surveils 3")
    void lifeAndSurveilMode() {
        Card topCard = new GrizzlyBears();
        Card middleCard = new SealOfStrength();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, middleCard, bottomCard));
        harness.setLife(player1, 10);

        castFangkeepersFamiliar(0);
        resolveCreatureAndEtb();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(middleCard, bottomCard);
    }

    @Test
    @DisplayName("ETB mode destroys target enchantment")
    void destroyEnchantmentMode() {
        harness.addToBattlefield(player2, new SealOfStrength());
        Permanent enchantment = gd.playerBattlefields.get(player2.getId()).getLast();

        castFangkeepersFamiliar(1, enchantment.getId());
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchantment);
    }

    @Test
    @DisplayName("Destroy mode rejects a creature target")
    void destroyModeRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getLast();

        assertThatThrownBy(() -> castFangkeepersFamiliar(1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target");
    }

    @Test
    @DisplayName("ETB mode counters a creature spell")
    void counterCreatureSpellMode() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new FangkeepersFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        FaerieInvaders creatureSpell = new FaerieInvaders();
        harness.setHand(player2, List.of(creatureSpell));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castCreature(player2, 0);

        harness.castCreature(player1, 0, 2, creatureSpell.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creatureSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Faerie Invaders");
    }

    private void castFangkeepersFamiliar(int mode) {
        castFangkeepersFamiliar(mode, null);
    }

    private void castFangkeepersFamiliar(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FangkeepersFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
