package com.github.laxika.magicalvibes.cards.b;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blitzball.class, BountyAgent.class, GrizzlyBears.class})
class BlitzballTest extends BaseCardTest {

    @Test
    @DisplayName("The mana ability adds one mana of a chosen color")
    void manaAbilityAddsChosenColor() {
        addReadyBlitzball();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The draw ability works after an opponent was dealt combat damage by a legendary creature")
    void drawsTwoCardsAfterLegendaryCreatureDealsCombatDamage() {
        Permanent blitzball = addReadyBlitzball();
        Permanent legendaryCreature = addCreatureReady(player2, new BountyAgent());
        recordCombatDamageToPlayer(legendaryCreature, player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blitzball);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blitzball.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("The draw ability requires combat damage from a legendary creature")
    void cannotDrawAfterNonlegendaryCombatDamage() {
        addReadyBlitzball();
        Permanent nonlegendaryCreature = addCreatureReady(player2, new GrizzlyBears());
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(nonlegendaryCreature.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Damage dealt to the ability controller does not satisfy the draw condition")
    void cannotDrawWhenLegendaryCreatureDamagedItsController() {
        addReadyBlitzball();
        Permanent legendaryCreature = addCreatureReady(player2, new BountyAgent());
        recordCombatDamageToPlayer(legendaryCreature, player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    private Permanent addReadyBlitzball() {
        return addCreatureReady(player1, new Blitzball());
    }

    private void recordCombatDamageToPlayer(Permanent source, com.github.laxika.magicalvibes.model.Player player) {
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(source.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(player.getId());
        gd.combatDamageSourcesWithLegendaryThisTurn.add(source.getId());
    }
}
