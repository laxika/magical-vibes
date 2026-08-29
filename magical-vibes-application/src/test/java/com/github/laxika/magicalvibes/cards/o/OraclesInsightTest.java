package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OraclesInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature taps to scry 1, then draw a card")
    void enchantedCreatureScriesThenDraws() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(creature);
        Card scriedCard = new Forest();
        Card drawnCard = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(scriedCard, drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(scriedCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("An unattached Aura grants no ability")
    void unattachedAuraGrantsNoAbility() {
        addCreatureReady(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new OraclesInsight()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The Aura can enchant only a creature")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new OraclesInsight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addAttachedAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OraclesInsight());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }
}
