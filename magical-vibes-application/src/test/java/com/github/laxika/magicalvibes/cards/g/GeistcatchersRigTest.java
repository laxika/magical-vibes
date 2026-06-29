package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.StormfrontPegasus;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GeistcatchersRigTest extends BaseCardTest {

    private void castRigAndAcceptMay(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GeistcatchersRig()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger, MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → target selection
        harness.handlePermanentChosen(player1, targetId); // choose target → deal 4 damage
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Card has MayEffect wrapping DealDamageToTargetCreatureEffect(4) on ETB")
    void hasCorrectEffects() {
        GeistcatchersRig card = new GeistcatchersRig();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(((DealDamageToTargetCreatureEffect) mayEffect.wrapped()).damage()).isEqualTo(4);
    }

    // ===== ETB deals 4 damage to creature with flying =====

    @Test
    @DisplayName("ETB deals 4 damage killing a creature with flying")
    void etbKillsFlyingCreature() {
        harness.addToBattlefield(player2, new StormfrontPegasus()); // 2/1 flying
        UUID pegasusId = harness.getPermanentId(player2, "Stormfront Pegasus");

        castRigAndAcceptMay(pegasusId);

        // Stormfront Pegasus is 2/1, 4 damage kills it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Stormfront Pegasus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Stormfront Pegasus"));
    }

    // ===== May ability declined =====

    @Test
    @DisplayName("Declining the may ability leaves the flying creature unharmed")
    void declineMayLeavesCreatureAlive() {
        harness.addToBattlefield(player2, new StormfrontPegasus());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GeistcatchersRig()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        // Stormfront Pegasus still on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Stormfront Pegasus"));

        // Rig still entered the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Geistcatcher's Rig"));
    }

    // ===== Geistcatcher's Rig enters as 4/5 =====

    @Test
    @DisplayName("Geistcatcher's Rig enters the battlefield as a 4/5")
    void rigEntersAs4_5() {
        harness.addToBattlefield(player1, new GeistcatchersRig());

        Permanent rig = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Geistcatcher's Rig"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, rig)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rig)).isEqualTo(5);
    }
}
