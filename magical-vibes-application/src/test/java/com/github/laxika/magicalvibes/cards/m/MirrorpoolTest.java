package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mirrorpool.class, AngelsMercy.class, GrizzlyBears.class})
class MirrorpoolTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and adds one colorless mana")
    void entersTappedAndAddsColorlessMana() {
        harness.setHand(player1, List.of(new Mirrorpool()));

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Copies an instant or sorcery spell you control")
    void copiesOwnInstantOrSorcerySpell() {
        Permanent mirrorpool = harness.addToBattlefieldAndReturn(player1, new Mirrorpool());
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0);
        harness.activateAbility(player1, 0, 1, null, mercy.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mirrorpool);
        harness.assertInGraveyard(player1, "Mirrorpool");
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .singleElement()
                .satisfies(copy -> assertThat(copy.getDescription()).isEqualTo("Copy of Angel's Mercy"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(34);
    }

    @Test
    @DisplayName("Creates a token copy of a creature you control")
    void createsTokenCopyOfOwnCreature() {
        Permanent mirrorpool = harness.addToBattlefieldAndReturn(player1, new Mirrorpool());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mirrorpool);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target an opponent's creature or a creature spell")
    void rejectsIllegalTargets() {
        harness.addToBattlefield(player1, new Mirrorpool());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);

        GrizzlyBears bearsSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(bearsSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bearsSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
