package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArtisanOfKozilek;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaestrosConfluence.class, GrizzlyBears.class, Shock.class, Ponder.class, ArtisanOfKozilek.class})
class MaestrosConfluenceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves all three different modes")
    void resolvesAllThreeModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));

        cast(new int[]{0, 1, 2}, List.of(spell.getId(), creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The -3/-3 mode chosen three times hits three creatures")
    void repeatedMinusThreeModeIsAppliedThreeTimes() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1, 1, 1}, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The goad mode makes the target player's creatures attack")
    void goadModeForcesTargetPlayersCreaturesToAttack() {
        addCreatureReady(player2, new GrizzlyBears());
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));

        cast(new int[]{0, 2, 2}, List.of(spell.getId(), player2.getId(), player1.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The graveyard mode can't target a creature card")
    void graveyardModeRejectsCreatureCards() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MaestrosConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 0);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, modes,
                null, null, List.of(creature.getId(), creature.getId(), creature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard mode chosen twice returns two different cards")
    void repeatedGraveyardModeReturnsDifferentInstantsAndSorceries() {
        Card first = new Shock();
        Card second = new Ponder();
        Card third = new Shock();
        harness.setGraveyard(player1, List.of(first, second, third));

        cast(new int[]{0, 0, 0}, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Ponder");
    }

    @Test
    @DisplayName("The graveyard mode chosen twice may target the same card")
    void repeatedGraveyardModeCanTargetTheSameCard() {
        Card spell = new Shock();
        harness.setGraveyard(player1, List.of(spell));

        cast(new int[]{0, 0, 0}, List.of(spell.getId(), spell.getId(), spell.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("The graveyard mode can't target a multicolored sorcery")
    void graveyardModeRejectsMulticoloredSorcery() {
        Card spell = new MaestrosConfluence();
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> cast(new int[]{0, 0, 0},
                List.of(spell.getId(), spell.getId(), spell.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard mode can't target an opponent's card")
    void graveyardModeRejectsOpponentsCard() {
        Card spell = new Shock();
        harness.setGraveyard(player2, List.of(spell));

        assertThatThrownBy(() -> cast(new int[]{0, 0, 0},
                List.of(spell.getId(), spell.getId(), spell.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The goad affects only the target player's creatures present at resolution")
    void goadAffectsOnlyTargetPlayersCreaturesPresentAtResolution() {
        Permanent affected = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(new int[]{2, 2, 2}, List.of(player2.getId(), player2.getId(), player2.getId()));
        harness.passBothPriorities();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.isGoaded(gd, affected)).isTrue();
        assertThat(gqs.isGoaded(gd, ownCreature)).isFalse();
        assertThat(gqs.isGoaded(gd, laterCreature)).isFalse();
    }

    @Test
    @DisplayName("The goad mode chosen three times for one player adds one attack requirement (CR 701.15d)")
    void goadModeChosenThriceAddsOneRequirement() {
        Permanent affected = addCreatureReady(player2, new GrizzlyBears());
        cast(new int[]{2, 2, 2}, List.of(player2.getId(), player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(als.getMustAttackRequirementCount(gd, affected)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -3/-3 mode chosen twice on one creature stacks")
    void repeatedMinusThreeModeStacksOnTheSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArtisanOfKozilek());

        cast(new int[]{1, 1, 2}, List.of(creature.getId(), creature.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Artisan of Kozilek");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.isGoaded(gd, creature)).isTrue();
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new MaestrosConfluence()));
        addMana();
        harness.castSorcery(player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices), targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
