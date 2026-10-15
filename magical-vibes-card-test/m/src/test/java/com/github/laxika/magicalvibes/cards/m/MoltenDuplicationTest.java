package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SundialOfTheInfinite;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenDuplication.class, GrizzlyBears.class, Manalith.class, SundialOfTheInfinite.class})
class MoltenDuplicationTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a creature you control, adds artifact, and grants haste")
    void copiesCreatureWithArtifactAndHaste() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDuplication(bears.getId());

        Permanent token = tokenCopy();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(harness.getGameQueryService().hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Copies an artifact you control")
    void copiesArtifact() {
        Permanent manalith = harness.addToBattlefieldAndReturn(player1, new Manalith());

        castDuplication(manalith.getId());

        Permanent token = tokenCopy();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isFalse();
        assertThat(harness.getGameQueryService().hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact or creature controlled by an opponent")
    void cannotTargetOpponentPermanent() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenDuplication()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature you control");
    }

    @Test
    @DisplayName("Sacrifices the token at the next end step")
    void sacrificesTokenAtNextEndStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDuplication(bears.getId());
        assertThat(tokenCopy()).isNotNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(tokenCopy()).isNotNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Haste expires when Sundial ends the turn with the sacrifice trigger on the stack")
    void hasteExpiresAfterEndingTurn() {
        harness.addToBattlefield(player1, new SundialOfTheInfinite());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDuplication(bears.getId());
        Permanent token = tokenCopy();
        assertThat(harness.getGameQueryService().hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(harness.getGameQueryService().hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot sacrifice the copy after another player gains control of it")
    void stolenTokenIsNotSacrificed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDuplication(bears.getId());
        Permanent token = tokenCopy();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);
        gd.stolenCreatures.put(token.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    @Test
    @DisplayName("Copy does not inherit tapped status, counters, or temporary power bonuses")
    void copiesOnlyCopiableCharacteristics() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setPowerModifier(3);
        bears.setToughnessModifier(3);

        castDuplication(bears.getId());

        Permanent token = tokenCopy();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.getPowerModifier()).isZero();
        assertThat(token.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Creates no copy when the target leaves the battlefield before resolution")
    void missingTargetCreatesNoToken() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenDuplication()));
        addMana();
        harness.castSorcery(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Molten Duplication");
    }

    private void castDuplication(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MoltenDuplication()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent tokenCopy() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No token copy was created"));
    }
}
