package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rageform.class, ArashinCleric.class, Forest.class, ReturnToTheEarth.class})
class RageformTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card, attaches Rageform, and grants double strike")
    void manifestsTopCardAndAttaches() {
        Permanent manifested = resolveRageform(new ArashinCleric());
        Permanent rageform = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isAura())
                .findFirst()
                .orElseThrow();

        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(rageform.isAttached()).isTrue();
        assertThat(rageform.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A manifested creature can turn face up and retains double strike")
    void manifestedCreatureTurnsFaceUpForManaCost() {
        Permanent manifested = resolveRageform(new ArashinCleric());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A manifested noncreature cannot turn face up")
    void manifestedNoncreatureCannotTurnFaceUp() {
        Permanent manifested = resolveRageform(new Forest());

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature card");
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Still manifests if Rageform is destroyed before its enter trigger resolves")
    void manifestsAfterSourceLeavesBattlefield() {
        harness.setLibrary(player1, List.of(new ArashinCleric()));
        harness.castFromHand(player1, new Rageform(), "{2}{R}{R}");
        harness.passBothPriorities();
        var rageformId = harness.getPermanentId(player1, "Rageform");

        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, rageformId);
        harness.assertInGraveyard(player1, "Rageform");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An empty library leaves Rageform unattached and puts it in the graveyard")
    void emptyLibraryPutsRageformInGraveyard() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Rageform(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Rageform");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rageform");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroying the Aura removes double strike and leaves the manifested creature")
    void destroyingAuraRemovesDoubleStrike() {
        Permanent manifested = resolveRageform(new ArashinCleric());
        harness.setHand(player1, List.of(new ReturnToTheEarth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Rageform"));

        harness.assertInGraveyard(player1, "Rageform");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(manifested);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent resolveRageform(Card topCard) {
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new Rageform(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }
}
