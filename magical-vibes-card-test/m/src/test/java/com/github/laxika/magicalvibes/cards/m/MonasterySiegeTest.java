package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MonasterySiege.class, Disfigure.class, LightningBolt.class, Naturalize.class,
        ZuranSpellcaster.class})
class MonasterySiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Khans draws an additional card, then makes the controller discard")
    void khansDrawsAndDiscardsDuringDrawStep() {
        castSiege(player1, "Khans");
        harness.setHand(player1, List.of(new Disfigure(), new Disfigure()));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Khans does not trigger during an opponent's draw step")
    void khansDoesNotTriggerDuringOpponentsDrawStep() {
        castSiege(player1, "Khans");
        int handBefore = gd.playerHands.get(player2.getId()).size();

        advanceToDraw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Dragons taxes an opponent's spell targeting the controller")
    void dragonsTaxesSpellTargetingController() {
        castSiege(player1, "Dragons");
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("Dragons taxes an opponent's spell targeting a permanent you control")
    void dragonsTaxesSpellTargetingControlledPermanent() {
        Permanent siege = castSiege(player1, "Dragons");
        prepareOpponentCast(new Naturalize(), ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, siege.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dragons grants no draw-step triggered ability")
    void dragonsDoesNotTriggerDuringDrawStep() {
        castSiege(player1, "Dragons");
        harness.setHand(player1, List.of());

        advanceToDraw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Dragons allows a spell after its additional two mana are paid")
    void dragonsAllowsSpellWhenTaxIsPaid() {
        castSiege(player1, "Dragons");
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Dragons does not tax the controller's own spells")
    void dragonsDoesNotTaxControllersSpells() {
        castSiege(player1, "Dragons");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Dragons does not tax spells targeting the opponent")
    void dragonsDoesNotTaxUnprotectedTargets() {
        castSiege(player1, "Dragons");
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Khans does not impose the Dragons targeting tax")
    void khansDoesNotTaxSpells() {
        castSiege(player1, "Khans");
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Two Dragons Sieges impose a total additional cost of four mana")
    void dragonsTaxesStackAcrossSieges() {
        castSiege(player1, "Dragons");
        castSiege(player1, "Dragons");
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("A Khans trigger still draws and discards after the Siege is destroyed")
    void khansTriggerSurvivesSourceRemoval() {
        Permanent siege = castSiege(player1, "Khans");
        harness.setHand(player1, List.of(new Naturalize()));
        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, siege.getId());
        harness.assertNotOnBattlefield(player1, "Monastery Siege");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Dragons does not tax activated abilities")
    void dragonsDoesNotTaxActivatedAbilities() {
        castSiege(player1, "Dragons");
        Permanent spellcaster = harness.addToBattlefieldAndReturn(player2, new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent castSiege(Player player, String mode) {
        harness.setHand(player, List.of(new MonasterySiege()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player, mode);
        return findPermanent(player, "Monastery Siege");
    }

    private void prepareOpponentCast(Card card, ManaColor color, int amount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(card));
        harness.addMana(player2, color, amount);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
