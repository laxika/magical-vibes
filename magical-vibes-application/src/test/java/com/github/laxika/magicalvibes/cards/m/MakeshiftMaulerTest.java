package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
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

@CardUsed({MakeshiftMauler.class, WalkingCorpse.class, ThinkTwice.class, Dissipate.class})
class MakeshiftMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Makeshift Mauler exiles a creature card from graveyard")
    void castingExilesCreatureFromGraveyard() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));

        harness.setHand(player1, List.of(new MakeshiftMauler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(MakeshiftMauler.class);

        // Creature card should be exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Cannot cast Makeshift Mauler without a creature in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setHand(player1, List.of(new MakeshiftMauler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card from graveyard for Makeshift Mauler")
    void cannotExileNonCreatureCard() {
        ThinkTwice instant = new ThinkTwice(); // Instant, not a creature
        harness.setGraveyard(player1, List.of(instant));

        harness.setHand(player1, List.of(new MakeshiftMauler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Makeshift Mauler resolves after paying its additional cost")
    void resolvesAfterPayingAdditionalCost() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));

        harness.setHand(player1, List.of(new MakeshiftMauler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Makeshift Mauler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exile cost is paid even if Makeshift Mauler is countered")
    void exileCostPaidEvenIfCountered() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));

        harness.setHand(player1, List.of(new MakeshiftMauler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        harness.setHand(player2, List.of(new Dissipate()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, gd.stack.getFirst().getCard().getId());
        harness.assertNotOnBattlefield(player1, "Makeshift Mauler");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof MakeshiftMauler);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
    }

    @Test
    @DisplayName("Can exile second creature from graveyard when multiple are present")
    void exilesCorrectCreatureByIndex() {
        WalkingCorpse corpse = new WalkingCorpse();
        ThinkTwice instant = new ThinkTwice(); // Non-creature, should stay
        WalkingCorpse secondCorpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse, instant, secondCorpse));

        harness.setHand(player1, List.of(new MakeshiftMauler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Exile the second Walking Corpse (index 2)
        harness.castCreatureWithGraveyardExile(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Makeshift Mauler");
        // Think Twice and first Walking Corpse remain in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(corpse, instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(secondCorpse);
        harness.assertInGraveyard(player1, "Think Twice");
    }

    @Test
    @DisplayName("Cannot omit the additional exile cost even with a creature available")
    void cannotOmitAdditionalCost() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));

        assertThatThrownBy(() -> harness.castFromHand(player1, new MakeshiftMauler(), "{3}{U}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exile");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(corpse);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Makeshift Mauler");
    }

    @Test
    @DisplayName("An opponent's creature card cannot pay the additional cost")
    void cannotExileOpponentsCreature() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player2, List.of(corpse));
        harness.setHand(player1, List.of(new MakeshiftMauler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(corpse);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Makeshift Mauler");
    }
}
