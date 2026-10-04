package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.p.PatientNaturalist;
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

@CardUsed({GhiredMirrorOfTheWilds.class, RaiseTheAlarm.class, PatientNaturalist.class})
class GhiredMirrorOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature copies a token that entered this turn")
    void createsTokenCopyOfTokenEnteredThisTurn() {
        Permanent ghired = addReadyGhired();
        List<Permanent> soldiers = createSoldiers();

        harness.activateAbility(player1, permanentIndex(ghired), null, soldiers.getFirst().getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(3);
    }

    @Test
    @DisplayName("A token from an earlier turn is not a legal target")
    void cannotTargetTokenThatEnteredEarlier() {
        Permanent ghired = addReadyGhired();
        List<Permanent> soldiers = createSoldiers();
        endTurn(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, permanentIndex(ghired), null, soldiers.getFirst().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ghired can use the granted tap ability on the turn it enters")
    void hasteAllowsImmediateActivation() {
        harness.castFromHand(player1, new GhiredMirrorOfTheWilds(), "{R}{G}{W}");
        harness.passBothPriorities();
        Permanent ghired = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent treasure = createTreasure(player1);

        harness.activateAbility(player1, permanentIndex(ghired), null, treasure.getId());
        harness.passBothPriorities();

        assertThat(ghired.isTapped()).isTrue();
        assertThat(tokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Other nontoken creatures can copy noncreature tokens")
    void grantsAbilityToOtherNontokenCreature() {
        addReadyGhired();
        Permanent treasure = createTreasure(player1);
        Permanent naturalist = findPermanent(player1, "Patient Naturalist");
        naturalist.setSummoningSick(false);

        harness.activateAbility(player1, permanentIndex(naturalist), null, treasure.getId());
        harness.passBothPriorities();

        assertThat(naturalist.isTapped()).isTrue();
        assertThat(tokens(player1)).hasSize(2);
        assertThat(tokens(player1)).allSatisfy(token -> assertThat(token.isTapped()).isFalse());
    }

    @Test
    @DisplayName("Token creatures do not receive Ghired's ability")
    void doesNotGrantAbilityToTokenCreatures() {
        addReadyGhired();
        List<Permanent> soldiers = createSoldiers();
        soldiers.getFirst().setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                permanentIndex(soldiers.getFirst()), null, soldiers.getLast().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent-controlled tokens are not legal targets")
    void cannotTargetOpponentsToken() {
        Permanent ghired = addReadyGhired();
        Permanent treasure = createTreasure(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                permanentIndex(ghired), null, treasure.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent creatures do not receive the ability")
    void doesNotGrantAbilityToOpponentsCreatures() {
        addReadyGhired();
        Permanent treasure = createTreasure(player2);
        Permanent naturalist = findPermanent(player2, "Patient Naturalist");
        naturalist.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(naturalist), null, treasure.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A copy created by Ghired is itself a legal target this turn")
    void canCopyNewlyCreatedCopy() {
        Permanent ghired = addReadyGhired();
        Permanent treasure = createTreasure(player1);
        Permanent naturalist = findPermanent(player1, "Patient Naturalist");
        naturalist.setSummoningSick(false);
        harness.activateAbility(player1, permanentIndex(ghired), null, treasure.getId());
        harness.passBothPriorities();
        Permanent copy = tokens(player1).stream()
                .filter(token -> !token.getId().equals(treasure.getId())).findFirst().orElseThrow();

        harness.activateAbility(player1, permanentIndex(naturalist), null, copy.getId());
        harness.passBothPriorities();

        assertThat(tokens(player1)).hasSize(3);
    }

    @Test
    @DisplayName("The ability does not copy a token sacrificed before resolution")
    void sacrificedTargetIsNotCopied() {
        Permanent ghired = addReadyGhired();
        Permanent treasure = createTreasure(player1);
        harness.activateAbility(player1, permanentIndex(ghired), null, treasure.getId());
        harness.activateAbility(player1, permanentIndex(treasure), null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.passBothPriorities();

        assertThat(tokens(player1)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted tap ability still obeys summoning sickness on other creatures")
    void otherCreaturesNeedToBeReady() {
        addReadyGhired();
        Permanent treasure = createTreasure(player1);
        Permanent naturalist = findPermanent(player1, "Patient Naturalist");

        assertThatThrownBy(() -> harness.activateAbility(player1,
                permanentIndex(naturalist), null, treasure.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nontoken permanent is not a legal copy target")
    void cannotTargetNontokenPermanent() {
        Permanent ghired = addReadyGhired();
        createTreasure(player1);
        Permanent naturalist = findPermanent(player1, "Patient Naturalist");

        assertThatThrownBy(() -> harness.activateAbility(player1,
                permanentIndex(ghired), null, naturalist.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent createTreasure(Player player) {
        harness.setLibrary(player, List.of());
        harness.enterBattlefieldAndReturn(player, new PatientNaturalist());
        harness.passBothPriorities();
        return tokens(player).getFirst();
    }

    private List<Permanent> tokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
    }

    private Permanent addReadyGhired() {
        Permanent ghired = addCreatureReady(player1, new GhiredMirrorOfTheWilds());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return ghired;
    }

    private List<Permanent> createSoldiers() {
        harness.castFromHand(player1, new RaiseTheAlarm(), "{1}{W}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
    }

    private int permanentIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
