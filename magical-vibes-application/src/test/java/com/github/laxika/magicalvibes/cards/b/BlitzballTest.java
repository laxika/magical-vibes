package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HillGigas;
import com.github.laxika.magicalvibes.cards.t.TellahGreatSage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blitzball.class, TellahGreatSage.class, HillGigas.class})
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
        Permanent legendaryCreature = addCreatureReady(player1, new TellahGreatSage());
        recordCombatDamageToPlayer(legendaryCreature, player2);
        harness.setLibrary(player1, List.of(new HillGigas(), new HillGigas()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blitzball);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blitzball.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof HillGigas);
    }

    @Test
    @DisplayName("The draw ability requires combat damage from a legendary creature")
    void cannotDrawAfterNonlegendaryCombatDamage() {
        addReadyBlitzball();
        Permanent nonlegendaryCreature = addCreatureReady(player1, new HillGigas());
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(nonlegendaryCreature.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Damage dealt to the ability controller does not satisfy the draw condition")
    void cannotDrawWhenLegendaryCreatureDamagedItsController() {
        addReadyBlitzball();
        Permanent legendaryCreature = addCreatureReady(player2, new TellahGreatSage());
        recordCombatDamageToPlayer(legendaryCreature, player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    private Permanent addReadyBlitzball() {
        return addCreatureReady(player1, new Blitzball());
    }

    @Test
    @DisplayName("Actual legendary combat damage enables drawing even after the creature leaves")
    void canDrawAfterCombatSourceLeavesBattlefield() {
        Permanent blitzball = addReadyBlitzball();
        Permanent attacker = addCreatureReady(player1, new TellahGreatSage());
        harness.setLibrary(player1, List.of(new HillGigas(), new HillGigas()));
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blitzball);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Actual nonlegendary combat damage to an opponent does not enable drawing")
    void cannotDrawAfterActualNonlegendaryCombatDamage() {
        Permanent blitzball = addReadyBlitzball();
        Permanent attacker = addCreatureReady(player1, new HillGigas());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
        assertThat(blitzball.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blitzball);
    }

    @Test
    @DisplayName("Drawing cannot be activated without qualifying damage")
    void cannotDrawBeforeCombatDamage() {
        Permanent blitzball = addReadyBlitzball();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
        assertThat(blitzball.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blitzball);
    }

    @Test
    @DisplayName("Legendary status must belong to the damage event that hit an opponent")
    void cannotCombineNonlegendaryDamageToOpponentWithLegendaryDamageToController() {
        Permanent blitzball = addReadyBlitzball();
        Permanent source = addCreatureReady(player2, new TellahGreatSage());
        // The same permanent previously hit player2 while nonlegendary, then became
        // legendary and changed controllers before hitting player1 in another combat.
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(source.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(player2.getId());
        recordCombatDamageToPlayer(source, player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
        assertThat(blitzball.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blitzball);
    }

    @Test
    @DisplayName("The mana ability taps Blitzball and resolves without using the stack")
    void manaAbilityDoesNotUseStackAndCannotBeRepeatedWhileTapped() {
        Permanent blitzball = harness.addToBattlefieldAndReturn(player1, new Blitzball());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.stack).isEmpty();
        assertThat(blitzball.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    private void recordCombatDamageToPlayer(Permanent source, com.github.laxika.magicalvibes.model.Player player) {
        gd.combatDamageToPlayersThisTurn
                .computeIfAbsent(source.getId(), ignored -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(player.getId());
        gd.combatDamageSourcesWithLegendaryThisTurn.add(source.getId());
    }
}
