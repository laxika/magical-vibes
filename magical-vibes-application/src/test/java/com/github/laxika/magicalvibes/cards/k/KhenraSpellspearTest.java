package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GitaxianSpellstalker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KhenraSpellspear.class, GitaxianSpellstalker.class, Shock.class})
class KhenraSpellspearTest extends BaseCardTest {

    @Test
    void prowessBoostsFrontFaceForNoncreatureSpell() {
        Permanent spellspear = addSpellspear();
        castShock();

        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(3);
    }

    @Test
    void transformsWithPhyrexianManaPaidWithLife() {
        Permanent spellspear = addSpellspear();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spellspear.isTransformed()).isTrue();
        assertThat(spellspear.getCard()).isInstanceOf(GitaxianSpellstalker.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void backFaceHasTwoSeparateProwessTriggers() {
        Permanent spellspear = addSpellspear();
        transform(spellspear);
        castShock();

        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(5);
    }

    @Test
    void backFaceWardCountersSpellWithoutPayment() {
        Permanent spellspear = addSpellspear();
        transform(spellspear);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spellspear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spellspear);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addSpellspear();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addSpellspear() {
        return harness.addToBattlefieldAndReturn(player1, new KhenraSpellspear());
    }

    private void transform(Permanent spellspear) {
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(spellspear.isTransformed()).isTrue();
    }

    private void castShock() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
