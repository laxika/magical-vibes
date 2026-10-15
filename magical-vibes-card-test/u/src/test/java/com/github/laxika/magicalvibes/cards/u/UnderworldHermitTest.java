package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.t.TragicFall;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderworldHermit.class, TragicFall.class})
class UnderworldHermitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates Squirrels equal to your black devotion, including this creature")
    void etbCreatesSquirrelsEqualToBlackDevotion() {
        harness.setHand(player1, List.of(new UnderworldHermit()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> squirrels = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .toList();

        assertThat(squirrels).hasSize(2);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(squirrel.getCard().getPower()).isEqualTo(1);
            assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Devotion counts only permanents controlled by the trigger's controller")
    void countsOnlyControlledPermanents() {
        harness.addToBattlefield(player1, new UnderworldHermit());
        harness.addToBattlefield(player2, new UnderworldHermit());
        harness.setGraveyard(player1, List.of(new UnderworldHermit()));
        harness.setExile(player1, List.of(new UnderworldHermit()));
        harness.setHand(player1, List.of(new UnderworldHermit(), new UnderworldHermit()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(squirrels()).hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Devotion is recalculated after the source leaves, and its trigger still resolves")
    void countsRemainingDevotionWhenSourceLeaves() {
        harness.addToBattlefield(player1, new UnderworldHermit());
        harness.setHand(player1, List.of(new UnderworldHermit(), new TragicFall()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertInGraveyard(player1, "Underworld Hermit");
        harness.passBothPriorities();

        assertThat(squirrels()).hasSize(2);
    }

    @Test
    @DisplayName("No tokens are created if black devotion is zero at resolution")
    void createsNoTokensWhenSourceLeavesAndDevotionIsZero() {
        harness.setHand(player1, List.of(new UnderworldHermit(), new TragicFall()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.assertInGraveyard(player1, "Underworld Hermit");
        harness.passBothPriorities();

        assertThat(squirrels()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> squirrels() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .toList();
    }
}
