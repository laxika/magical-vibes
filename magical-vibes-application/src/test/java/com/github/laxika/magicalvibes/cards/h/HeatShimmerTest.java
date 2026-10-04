package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.s.Shapesharer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeatShimmer.class, AxegrinderGiant.class, Lignify.class, Shapesharer.class})
class HeatShimmerTest extends BaseCardTest {

    private void castHeatShimmer(UUID targetId) {
        harness.setHand(player1, List.of(new HeatShimmer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent token(UUID controllerId) {
        return gd.playerBattlefields.get(controllerId).stream()
                .filter(p -> p.getCard().getName().equals("Axegrinder Giant") && p.getCard().isToken())
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Creates a token copy of target creature with haste")
    void createsHastyTokenCopy() {
        harness.addToBattlefield(player1, new AxegrinderGiant());
        castHeatShimmer(harness.getPermanentId(player1, "Axegrinder Giant"));

        assertThat(token(player1.getId()).getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new AxegrinderGiant());
        castHeatShimmer(harness.getPermanentId(player2, "Axegrinder Giant"));

        // Token enters under the caster's control.
        assertThat(token(player1.getId())).isNotNull();
    }

    @Test
    @DisplayName("Token can attack the turn it is created thanks to haste")
    void tokenCanAttackDueToHaste() {
        harness.addToBattlefield(player1, new AxegrinderGiant());
        castHeatShimmer(harness.getPermanentId(player1, "Axegrinder Giant"));

        Permanent tokenPermanent = token(player1.getId());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tokenPermanent);

        declareAttackers(player1, List.of(attackerIndex));

        assertThat(tokenPermanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Token is exiled at the beginning of the next end step")
    void tokenExiledAtEndStep() {
        harness.addToBattlefield(player1, new AxegrinderGiant());
        castHeatShimmer(harness.getPermanentId(player1, "Axegrinder Giant"));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Axegrinder Giant") && p.getCard().isToken());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Axegrinder Giant") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Original creature remains on the battlefield")
    void originalCreatureRemains() {
        harness.addToBattlefield(player1, new AxegrinderGiant());
        castHeatShimmer(harness.getPermanentId(player1, "Axegrinder Giant"));

        long count = countPermanents(player1, "Axegrinder Giant");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void losingAbilitiesBeforeEndStepPreventsExile() {
        harness.addToBattlefield(player1, new AxegrinderGiant());
        castHeatShimmer(harness.getPermanentId(player1, "Axegrinder Giant"));
        Permanent copy = token(player1.getId());
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, copy.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy);
    }

    @Test
    void creatureCopyingTokenInheritsEndStepExileAbility() {
        harness.addToBattlefield(player1, new AxegrinderGiant());
        Permanent shapesharer = addCreatureReady(player1, new Shapesharer());
        castHeatShimmer(harness.getPermanentId(player1, "Axegrinder Giant"));
        Permanent copy = token(player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 3);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(shapesharer);
        harness.activateAbilityWithMultiTargets(player1, index, 0,
                List.of(shapesharer.getId(), copy.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shapesharer, copy);
    }
}
