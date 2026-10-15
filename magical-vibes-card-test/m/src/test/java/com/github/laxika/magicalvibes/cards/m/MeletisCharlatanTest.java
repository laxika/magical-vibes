package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeletisCharlatan.class, CounselOfTheSoratami.class, GrizzlyBears.class, LightningBolt.class, LightningStrike.class})
class MeletisCharlatanTest extends BaseCardTest {

    private int addCharlatan() {
        Permanent charlatan = addCreatureReady(player2, new MeletisCharlatan());
        harness.addMana(player2, ManaColor.BLUE, 3);
        return gd.playerBattlefields.get(player2.getId()).indexOf(charlatan);
    }

    @Test
    @DisplayName("Copies an instant or sorcery under that spell's controller")
    void copiesForTargetSpellController() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int charlatanIdx = addCharlatan();
        int player1HandBeforeCopy = gd.playerHands.get(player1.getId()).size();
        int player2HandBeforeCopy = gd.playerHands.get(player2.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, charlatanIdx, null, counsel.getId());
        harness.passBothPriorities();

        StackEntry copy = gd.stack.stream()
                .filter(StackEntry::isCopy)
                .findFirst()
                .orElseThrow();
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size() - player1HandBeforeCopy).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(player2HandBeforeCopy);
    }

    @Test
    @DisplayName("The target spell's controller receives the retarget choice")
    void targetSpellControllerChoosesNewTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        int charlatanIdx = addCharlatan();

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, charlatanIdx, null, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int charlatanIdx = addCharlatan();
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, charlatanIdx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining new targets still creates a copy and resolves both spells")
    void decliningRetargetKeepsOriginalTarget() {
        LightningStrike strike = new LightningStrike();
        harness.setHand(player1, List.of(strike));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);
        int charlatanIdx = addCharlatan();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, charlatanIdx, null, strike.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(strike);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Changing the copy's target leaves the original spell's target unchanged")
    void changingCopyTargetDoesNotChangeOriginal() {
        LightningStrike strike = new LightningStrike();
        harness.setHand(player1, List.of(strike));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        int charlatanIdx = addCharlatan();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, charlatanIdx, null, strike.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability taps Charlatan and spends all three available mana")
    void paysManaAndTapCosts() {
        LightningStrike strike = new LightningStrike();
        harness.setHand(player1, List.of(strike));
        harness.addMana(player1, ManaColor.RED, 2);
        int charlatanIdx = addCharlatan();

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2, charlatanIdx, null, strike.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()).get(charlatanIdx).isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Charlatan cannot activate its tap ability")
    void summoningSicknessPreventsActivation() {
        LightningStrike strike = new LightningStrike();
        harness.setHand(player1, List.of(strike));
        harness.addMana(player1, ManaColor.RED, 2);
        int charlatanIdx = addCharlatan();
        gd.playerBattlefields.get(player2.getId()).get(charlatanIdx).setSummoningSick(true);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, charlatanIdx, null, strike.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }
}
