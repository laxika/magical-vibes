package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LlanowarScout;
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

@CardUsed({VodalianArcanist.class, Divination.class, LlanowarScout.class, BlinkOfAnEye.class, IcyManipulator.class})
class VodalianArcanistTest extends BaseCardTest {

    @Test
    void manaAbilityResolvesImmediatelyAndTapsSource() {
        Permanent arcanist = addCreatureReady(player1, new VodalianArcanist());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(arcanist.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);
    }

    @Test
    void summoningSickArcanistCannotProduceMana() {
        Permanent arcanist = harness.addToBattlefieldAndReturn(player1, new VodalianArcanist());
        arcanist.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(arcanist.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isZero();
    }

    @Test
    void restrictedManaPaysForInstant() {
        addCreatureReady(player1, new VodalianArcanist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new BlinkOfAnEye()));

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
    }

    @Test
    void restrictedColorlessCannotPayColoredInstantCost() {
        addCreatureReady(player1, new VodalianArcanist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new BlinkOfAnEye()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void restrictedManaCannotPayForActivatedAbility() {
        Permanent arcanist = addCreatureReady(player1, new VodalianArcanist());
        Permanent icy = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, arcanist.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(icy.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability adds one instant/sorcery-only colorless mana")
    void tapAbilityAddsRestrictedColorless() {
        addCreatureReady(player1, new VodalianArcanist());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted colorless can pay generic cost of a sorcery spell")
    void restrictedColorlessPaysForSorcerySpell() {
        addCreatureReady(player1, new VodalianArcanist());

        // Activate ability: 1 instant/sorcery-only colorless
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Pool: 1 instant/sorcery-only colorless + 1 blue + 1 colorless = enough for {2}{U}
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Divination()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // The restricted mana pays part of the generic cost.
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Restricted colorless is not spent when casting a creature spell")
    void restrictedColorlessNotUsedForCreatureSpell() {
        addCreatureReady(player1, new VodalianArcanist());

        // Activate ability: 1 instant/sorcery-only colorless
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast Llanowar Scout ({1}{G}) with regular mana only
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new LlanowarScout()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Llanowar Scout enters the battlefield using regular green mana
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        // Instant/sorcery-only colorless should be untouched
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot cast creature spell if only restricted colorless available for generic cost")
    void cannotCastCreatureWithOnlyRestrictedColorless() {
        addCreatureReady(player1, new VodalianArcanist());

        // Activate ability: 1 instant/sorcery-only colorless
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Try to cast Llanowar Scout ({1}{G}) with only 1 green + 1 restricted colorless
        // Should fail because restricted colorless can't pay for creature spells
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new LlanowarScout()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted colorless drains at phase transition")
    void restrictedColorlessDrainsAtPhaseTransition() {
        addCreatureReady(player1, new VodalianArcanist());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(1);

        // Drain non-persistent mana (simulates phase transition)
        gd.playerManaPools.get(player1.getId()).drainNonPersistent();

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColorless()).isEqualTo(0);
    }
}
