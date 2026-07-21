package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Fatestitcher")
class FatestitcherTest extends BaseCardTest {

    // ===== {T}: tap or untap another target permanent =====

    @Test
    @DisplayName("Taps an untapped target permanent")
    void tapsUntappedPermanent() {
        addReadyFatestitcher(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped target permanent")
    void untapsTappedPermanent() {
        addReadyFatestitcher(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself (another permanent)")
    void cannotTargetItself() {
        Permanent fatestitcher = addReadyFatestitcher(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fatestitcher.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another permanent");
    }

    @Test
    @DisplayName("Taps an untapped land (another permanent, not only creatures)")
    void tapsUntappedLand() {
        addReadyFatestitcher(player1);
        Permanent land = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
    }

    // ===== Unearth {U} =====

    @Test
    @DisplayName("Unearth returns Fatestitcher to the battlefield with haste")
    void unearthReturnsWithHaste() {
        Fatestitcher fatestitcher = new Fatestitcher();
        harness.setGraveyard(player1, List.of(fatestitcher));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fatestitcher"))
                .findFirst().orElseThrow();
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Fatestitcher"));
    }

    @Test
    @DisplayName("Unearthed Fatestitcher is exiled at the next end step")
    void unearthExiledAtEndStep() {
        Fatestitcher fatestitcher = new Fatestitcher();
        harness.setGraveyard(player1, List.of(fatestitcher));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fatestitcher"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fatestitcher"));
    }

    @Test
    @DisplayName("Unearth can only be activated at sorcery speed")
    void unearthOnlyAtSorcerySpeed() {
        Fatestitcher fatestitcher = new Fatestitcher();
        harness.setGraveyard(player1, List.of(fatestitcher));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fatestitcher"));
    }

    @Test
    @DisplayName("Unearthed Fatestitcher is exiled if it would leave the battlefield")
    void unearthExiledIfWouldLeaveBattlefield() {
        Fatestitcher fatestitcher = new Fatestitcher();
        harness.setGraveyard(player1, List.of(fatestitcher));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fatestitcher"))
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, perm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fatestitcher"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Fatestitcher"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fatestitcher"));
    }

    // ===== Helpers =====

    private Permanent addReadyFatestitcher(Player player) {
        Fatestitcher card = new Fatestitcher();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Card card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Forest card = new Forest();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
