package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VillageRites.class, WalkingCorpse.class, Swamp.class})
class VillageRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Village Rites sacrifices a creature and puts spell on stack")
    void castingSacrificesCreatureAndPutsOnStack() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new VillageRites()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Village Rites");

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Resolving Village Rites draws two cards")
    void resolvingDrawsTwoCards() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new VillageRites()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        harness.assertInGraveyard(player1, "Village Rites");
    }

    @Test
    @DisplayName("Cannot cast Village Rites without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new VillageRites()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature for Village Rites")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new VillageRites()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature for Village Rites even when a creature is available")
    void cannotSacrificeNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new VillageRites()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a creature");

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
