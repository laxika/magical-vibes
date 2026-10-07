package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrimGuardian;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Twinflame.class, GrizzlyBears.class, HillGiant.class, Clone.class, GrimGuardian.class})
class TwinflameTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty token copy for each target creature you control")
    void createsHastyTokenCopyForEachTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        castTwinflame(List.of(bears.getId(), giant.getId()), 2, 3);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Hill Giant");
        assertThat(tokens).allMatch(permanent -> harness.getGameQueryService().hasKeyword(gd, permanent, Keyword.HASTE));
    }

    @Test
    @DisplayName("Exiles the token copies at the beginning of the next end step")
    void exilesTokenCopiesAtNextEndStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTwinflame(List.of(bears.getId()), 1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        harness.setHand(player1, List.of(new Twinflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Strive requires {2}{R} for each additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Twinflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only creatures you control")
    void targetsMustBeCreaturesYouControl() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twinflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("A creature copying a Twinflame token inherits haste and is not exiled")
    void copyingTokenInheritsHasteWithoutExile() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTwinflame(List.of(bears.getId()), 1, 1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        Clone cloneCard = new Clone();
        harness.castFromHand(player1, cloneCard, "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());
        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElseThrow();

        assertThat(harness.getGameQueryService().hasKeyword(gd, clone, Keyword.HASTE)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clone, bears).doesNotContain(token);
    }

    @Test
    @DisplayName("All token copies see each other enter the battlefield")
    void tokenCopiesSeeEachOtherEnter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrimGuardian());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrimGuardian());
        castTwinflame(List.of(first.getId(), second.getId()), 2, 3);

        assertThat(gd.stack).hasSize(8);
        for (int i = 0; i < 8; i++) {
            harness.passBothPriorities();
        }
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("One delayed triggered ability exiles all Twinflame tokens together")
    void singleDelayedTriggerExilesAllTokens() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        castTwinflame(List.of(bears.getId(), giant.getId()), 2, 3);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactlyInAnyOrder(bears, giant);
    }

    private void castTwinflame(List<UUID> targetIds, int redMana, int colorlessMana) {
        harness.setHand(player1, List.of(new Twinflame()));
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
