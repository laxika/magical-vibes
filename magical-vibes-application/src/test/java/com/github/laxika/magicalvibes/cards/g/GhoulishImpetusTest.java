package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhoulishImpetus.class, GrizzlyBears.class, DoomBlade.class})
class GhoulishImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the enchanted creature and grants deathtouch")
    void boostsAndGrantsDeathtouch() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Goads the enchanted creature while the Aura remains attached")
    void goadsEnchantedCreature() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());
        attachAura(player2, creature);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Returns to the battlefield attached to a legal creature at the next end step")
    void returnsAtNextEndStep() {
        Permanent dyingCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent targetCreature = addReadyCreature(player2, new GrizzlyBears());
        Card auraCard = new GhoulishImpetus();
        Permanent aura = attachAura(player1, dyingCreature, auraCard);

        destroyCreature(dyingCreature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(auraCard.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returnedAura = findPermanent(player1, auraCard.getId());
        assertThat(returnedAura).isNotNull();
        assertThat(returnedAura.getAttachedTo()).isEqualTo(targetCreature.getId());
        assertThat(aura.getId()).isNotEqualTo(returnedAura.getId());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        return attachAura(controller, creature, new GhoulishImpetus());
    }

    private Permanent attachAura(Player controller, Permanent creature, Card card) {
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void destroyCreature(UUID creatureId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creatureId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Player player, UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
