package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.m.Mulch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnburialRites.class, WalkingCorpse.class, Mulch.class})
class UnburialRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from your graveyard to the battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new UnburialRites()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
        harness.assertNotInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureCard() {
        Card nonCreature = new Mulch();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new UnburialRites()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target card in opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new UnburialRites()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fizzles if target creature leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new UnburialRites()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new UnburialRites()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unburial Rites");
    }

    @Test
    @DisplayName("Flashback returns creature from graveyard to battlefield")
    void flashbackReturnsCreatureToBattlefield() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(new UnburialRites(), creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Flashback exiles Unburial Rites instead of going to graveyard")
    void flashbackExilesAfterResolving() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(new UnburialRites(), creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Unburial Rites");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Unburial Rites"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as sorcery")
    void flashbackPutsOnStackAsSorcery() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(new UnburialRites(), creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Unburial Rites");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(new UnburialRites(), creature));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback requires white mana even when enough black mana is available")
    void flashbackRequiresWhiteMana() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(new UnburialRites(), creature));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Unburial Rites");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback exiles the spell even when its target leaves the graveyard")
    void flashbackExilesWhenTargetBecomesIllegal() {
        Card creature = new WalkingCorpse();
        Card rites = new UnburialRites();
        harness.setGraveyard(player1, List.of(rites, creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertNotInGraveyard(player1, "Unburial Rites");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(rites.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without a creature target")
    void requiresTarget() {
        harness.setHand(player1, List.of(new UnburialRites()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Unburial Rites");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback cannot target an opponent's graveyard")
    void flashbackCannotTargetOpponentGraveyard() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(new UnburialRites()));
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Unburial Rites");
        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Flashback still requires sorcery timing")
    void flashbackCannotBeCastDuringEndStep() {
        Card creature = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(new UnburialRites(), creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gd.currentStep = TurnStep.END_STEP;

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Unburial Rites");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback cannot target a non-creature card")
    void flashbackCannotTargetNonCreature() {
        Card nonCreature = new Mulch();
        harness.setGraveyard(player1, List.of(new UnburialRites(), nonCreature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Unburial Rites");
        assertThat(gd.stack).isEmpty();
    }
}
