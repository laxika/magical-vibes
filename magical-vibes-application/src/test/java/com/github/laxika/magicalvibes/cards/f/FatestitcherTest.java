package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Fatestitcher")
@CardUsed({Fatestitcher.class, CylianElf.class, Forest.class, Terminate.class})
class FatestitcherTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped target permanent")
    void tapsUntappedPermanent() {
        addReadyFatestitcher(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

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
        harness.handleMayAbilityChosen(player1, true);

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
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unearth returns Fatestitcher to the battlefield with haste")
    void unearthReturnsWithHaste() {
        Fatestitcher fatestitcher = new Fatestitcher();
        harness.setGraveyard(player1, List.of(fatestitcher));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Fatestitcher");
        assertThat(gqs.hasKeyword(gd, perm, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Fatestitcher");
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

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Fatestitcher");
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

        harness.assertInGraveyard(player1, "Fatestitcher");
    }

    @Test
    @DisplayName("Unearthed Fatestitcher is exiled if it would leave the battlefield")
    void unearthExiledIfWouldLeaveBattlefield() {
        Fatestitcher fatestitcher = new Fatestitcher();
        harness.setGraveyard(player1, List.of(fatestitcher));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Fatestitcher");

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, perm.getId());

        harness.assertNotOnBattlefield(player1, "Fatestitcher");
        harness.assertNotInGraveyard(player1, "Fatestitcher");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fatestitcher"));
    }

    @Test
    @DisplayName("Controller may decline tapping an untapped target")
    void mayDeclineTappingTarget() {
        Permanent source = addReadyFatestitcher(player1);
        Permanent target = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller may decline untapping a tapped target")
    void mayDeclineUntappingTarget() {
        addReadyFatestitcher(player1);
        Permanent target = addReadyLand(player2);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Newly entered Fatestitcher cannot pay its tap cost without haste")
    void summoningSicknessPreventsActivation() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Fatestitcher());
        Permanent target = addReadyLand(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unearth haste permits immediately paying the tap cost")
    void unearthPermitsImmediateActivation() {
        harness.setGraveyard(player1, List.of(new Fatestitcher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent target = addReadyLand(player2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Fatestitcher");

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Unearth requires blue mana")
    void unearthCannotBeActivatedWithoutMana() {
        harness.setGraveyard(player1, List.of(new Fatestitcher()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Fatestitcher");
        harness.assertNotOnBattlefield(player1, "Fatestitcher");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unearth cannot be activated with an ability on the stack")
    void unearthRequiresEmptyStack() {
        addReadyFatestitcher(player1);
        Permanent target = addReadyLand(player2);
        harness.setGraveyard(player1, List.of(new Fatestitcher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Fatestitcher");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Unearth returns only the activated card, not another copy")
    void unearthReturnsOnlyActivatedCard() {
        Fatestitcher first = new Fatestitcher();
        Fatestitcher second = new Fatestitcher();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fatestitcher").getCard().getId()).isEqualTo(second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(c -> c.getId())
                .containsExactly(first.getId());
        assertThat(countPermanents(player1, "Fatestitcher")).isEqualTo(1);
    }

    @Test
    @DisplayName("Unearth does nothing if its card leaves the graveyard before resolution")
    void unearthDoesNothingIfSourceLeavesGraveyard() {
        Fatestitcher card = new Fatestitcher();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        gd.playerGraveyards.get(player1.getId()).remove(card);
        gd.getPlayerExiledCards(player1.getId()).add(card);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fatestitcher");
        harness.assertNotInGraveyard(player1, "Fatestitcher");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    private Permanent addReadyFatestitcher(Player player) {
        return addCreatureReady(player, new Fatestitcher());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new CylianElf());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
