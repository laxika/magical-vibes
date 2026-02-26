package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubtypeSacrificeCostHandlerTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-selects single matching creature for sacrifice")
    void autoSelectsSingleCreature() {
        Permanent source = addReadyPermanent(player1, createCardWithSubtypeSacrificeCost());
        addReadyPermanent(player1, createGoblinCreature("Goblin Recruit"));

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, idx, null, player2.getId());

        // Single goblin should be auto-sacrificed, no prompt
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Recruit"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("No matching subtype creature throws")
    void noMatchingSubtypeCreatureThrows() {
        Permanent source = addReadyPermanent(player1, createCardWithSubtypeSacrificeCost());
        addReadyPermanent(player1, createNonGoblinCreature("Human Soldier"));
        harness.addMana(player1, ManaColor.RED, 2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Goblin");
    }

    @Test
    @DisplayName("Multiple matching creatures prompt for choice")
    void multipleMatchingCreaturesPromptChoice() {
        Permanent source = addReadyPermanent(player1, createCardWithSubtypeSacrificeCost());
        addReadyPermanent(player1, createGoblinCreature("Goblin A"));
        addReadyPermanent(player1, createGoblinCreature("Goblin B"));
        harness.addMana(player1, ManaColor.RED, 2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, player2.getId());

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Completing sacrifice choice puts ability on stack")
    void completingSacrificeChoicePutsAbilityOnStack() {
        Permanent source = addReadyPermanent(player1, createCardWithSubtypeSacrificeCost());
        Permanent goblinA = addReadyPermanent(player1, createGoblinCreature("Goblin A"));
        addReadyPermanent(player1, createGoblinCreature("Goblin B"));
        harness.addMana(player1, ManaColor.RED, 2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, idx, null, player2.getId());
        harness.handlePermanentChosen(player1, goblinA.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin A"));
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createCardWithSubtypeSacrificeCost() {
        Card card = new Card();
        card.setName("Test Goblin Sacrificer");
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setPower(2);
        card.setToughness(2);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{1}{R}",
                List.of(new SacrificeSubtypeCreatureCost(CardSubtype.GOBLIN), new DealDamageToAnyTargetEffect(2)),
                "Sacrifice a Goblin: deal 2 damage to any target"
        ));
        return card;
    }

    private Card createGoblinCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.GOBLIN));
        return card;
    }

    private Card createNonGoblinCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{W}");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        return card;
    }
}
