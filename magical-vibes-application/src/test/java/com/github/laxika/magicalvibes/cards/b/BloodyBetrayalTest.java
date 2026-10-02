package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({BloodyBetrayal.class, GrizzlyBears.class, Pacifism.class, Abrade.class})
class BloodyBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, grants haste, and creates a Blood token")
    void resolvesAllEffects() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        castBloodyBetrayal(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        List<Permanent> bloodTokens = findPermanents(player1, "Blood");
        assertThat(bloodTokens).hasSize(1);
        assertThat(bloodTokens.getFirst().getCard().isToken()).isTrue();
        assertThat(bloodTokens.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(bloodTokens.getFirst().getCard().getSubtypes()).contains(CardSubtype.BLOOD);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castBloodyBetrayal(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new BloodyBetrayal()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target a creature already controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();

        castBloodyBetrayal(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no Blood token when the target dies in response")
    void createsNoBloodWhenTargetDiesInResponse() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloodyBetrayal()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bloody Betrayal");
        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    @DisplayName("The stolen creature can attack immediately")
    void stolenCreatureCanAttackImmediately() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        castBloodyBetrayal(target);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    private void castBloodyBetrayal(Permanent target) {
        harness.setHand(player1, List.of(new BloodyBetrayal()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
