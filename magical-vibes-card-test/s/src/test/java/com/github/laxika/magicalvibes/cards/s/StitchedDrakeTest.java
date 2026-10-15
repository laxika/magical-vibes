package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.d.DreamTwist;
import com.github.laxika.magicalvibes.cards.d.Dissipate;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StitchedDrake.class, WalkingCorpse.class, DreamTwist.class, Dissipate.class})
class StitchedDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Stitched Drake exiles a creature card from graveyard")
    void castingExilesCreatureFromGraveyard() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(StitchedDrake.class);

        // Creature card should be exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Cannot cast Stitched Drake without a creature in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card from graveyard for Stitched Drake")
    void cannotExileNonCreatureCard() {
        DreamTwist dreamTwist = new DreamTwist(); // Instant, not a creature
        harness.setGraveyard(player1, List.of(dreamTwist));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Stitched Drake resolves after paying its additional cost")
    void resolvesAfterPayingAdditionalCost() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stitched Drake");
    }

    @Test
    @DisplayName("Exile cost is paid even if Stitched Drake is countered")
    void exileCostPaidEvenIfCountered() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player2, 0, gd.stack.getFirst().getCard().getId());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Stitched Drake");

        // Exile cost already paid
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Can exile second creature from graveyard when multiple are present")
    void exilesCorrectCreatureByIndex() {
        WalkingCorpse corpse = new WalkingCorpse();
        DreamTwist dreamTwist = new DreamTwist(); // Non-creature, should stay
        WalkingCorpse secondCorpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse, dreamTwist, secondCorpse));

        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Exile the second Walking Corpse (index 2)
        harness.castCreatureWithGraveyardExile(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stitched Drake");
        // Dream Twist and first Walking Corpse remain in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(corpse, dreamTwist);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(secondCorpse);
    }

    @Test
    @DisplayName("A creature in the opponent's graveyard cannot pay the additional cost")
    void cannotUseOpponentsGraveyard() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(corpse));
        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(corpse);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stitched Drake");
    }

    @Test
    @DisplayName("A creature in the graveyard does not let the caster omit the exile cost")
    void cannotOmitExileCost() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));
        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(corpse);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stitched Drake");
    }

    @Test
    @DisplayName("Insufficient mana does not exile the selected creature")
    void insufficientManaDoesNotPayExileCost() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));
        harness.setHand(player1, List.of(new StitchedDrake()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(corpse);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Stitched Drake");
    }
}
