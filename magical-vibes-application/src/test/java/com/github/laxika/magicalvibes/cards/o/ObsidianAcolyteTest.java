package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.Annihilate;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({ObsidianAcolyte.class, Annihilate.class, Plains.class, RazorfootGriffin.class})
class ObsidianAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Has protection from black")
    void hasProtectionFromBlack() {
        Permanent acolyte = addCreatureReady(player1, new ObsidianAcolyte());

        harness.setHand(player2, List.of(new Annihilate()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, acolyte.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{W}: target creature gains protection from black until end of turn")
    void grantsProtectionFromBlackToTargetCreature() {
        addCreatureReady(player1, new ObsidianAcolyte());
        Permanent griffin = addCreatureReady(player1, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, griffin.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, griffin, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("The ability can target a creature an opponent controls")
    void grantsProtectionToOpponentsCreature() {
        addCreatureReady(player1, new ObsidianAcolyte());
        Permanent griffin = addCreatureReady(player2, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, griffin.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, griffin, CardColor.BLACK)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new ObsidianAcolyte());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted protection wears off at end of turn")
    void grantedProtectionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ObsidianAcolyte());
        Permanent griffin = addCreatureReady(player1, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, griffin.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, griffin, CardColor.BLACK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, griffin, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection from black stops black removal after the ability resolves")
    void grantedProtectionStopsBlackRemoval() {
        addCreatureReady(player1, new ObsidianAcolyte());
        Permanent griffin = addCreatureReady(player1, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, griffin.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Annihilate()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, griffin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
