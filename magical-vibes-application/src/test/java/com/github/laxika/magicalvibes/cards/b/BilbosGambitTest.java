package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BilbosGambit.class, GrizzlyBears.class, MightOfOaks.class})
class BilbosGambitTest extends BaseCardTest {

    @Test
    void withoutGiftReturnsTargetSpellToItsOwnersHand() {
        UUID targetSpellId = castMightOfOaks();

        castBilbosGambit(targetSpellId, false);

        harness.assertInHand(player2, "Might of Oaks");
        assertThat(gd.playersSilencedThisTurn).isEmpty();
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    void withGiftReturnsSpellCreatesTreasureAndPreventsAllPlayersFromCasting() {
        UUID targetSpellId = castMightOfOaks();

        castBilbosGambit(targetSpellId, true);

        harness.assertInHand(player2, "Might of Oaks");
        harness.assertOnBattlefield(player2, "Treasure");
        assertThat(gd.playersSilencedThisTurn).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canReturnOwnCreatureSpellWhileGivingTreasureToOpponent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        castBilbosGambit(bears.getId(), true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Treasure");
        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void invalidTargetPreventsBothGiftAndCastingRestriction() {
        UUID targetSpellId = castMightOfOaks();
        harness.setHand(player1, List.of(new BilbosGambit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithGift(player1, 0, targetSpellId, true);

        harness.setHand(player2, List.of(new BilbosGambit()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstantWithGift(player2, 0, targetSpellId, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Might of Oaks");
        harness.assertInGraveyard(player1, "Bilbo's Gambit");
        harness.assertInGraveyard(player2, "Bilbo's Gambit");
        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.assertNotOnBattlefield(player2, "Treasure");
        assertThat(gd.playersSilencedThisTurn).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void promisedGiftPreventsOpponentFromCastingReturnedSpell() {
        UUID targetSpellId = castMightOfOaks();
        castBilbosGambit(targetSpellId, true);

        harness.addMana(player2, ManaColor.GREEN, 4);
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Might of Oaks");
    }

    private UUID castMightOfOaks() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player2, List.of(might));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.castInstant(player2, 0, target.getId());
        return might.getId();
    }

    private void castBilbosGambit(UUID targetSpellId, boolean giftPromised) {
        harness.setHand(player1, List.of(new BilbosGambit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithGift(player1, 0, targetSpellId, giftPromised);
        harness.passBothPriorities();
    }
}
