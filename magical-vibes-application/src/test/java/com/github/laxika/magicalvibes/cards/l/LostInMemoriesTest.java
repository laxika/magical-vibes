package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LostInMemories.class, GrizzlyBears.class, GiantGrowth.class})
class LostInMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Lost in Memories boosts the enchanted creature")
    void boostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage grants flashback to an instant or sorcery in the controller's graveyard")
    void combatDamageGrantsFlashback() {
        GiantGrowth spell = new GiantGrowth();
        harness.setGraveyard(player1, List.of(spell));
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).containsExactly(spell.getId());
    }

    @Test
    @DisplayName("The granted flashback uses the card's mana cost")
    void grantedFlashbackUsesManaCost() {
        GiantGrowth spell = new GiantGrowth();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setGraveyard(player1, List.of(spell));
        attachAura(creature);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Lost in Memories cannot enchant an opponent's creature")
    void cannotEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LostInMemories()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void attachAura(Permanent creature) {
        harness.setHand(player1, List.of(new LostInMemories()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
