package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.h.Hydrosurge;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheTendedGarden;
import com.github.laxika.magicalvibes.cards.r.RavingOniSlave;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ONaginata.class, ArabaMothrider.class, KamiOfTheTendedGarden.class, RavingOniSlave.class})
class ONaginataTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new KamiOfTheTendedGarden());
        Permanent naginata = addReadyNaginata(player1);
        naginata.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void equipAttachesToCreatureWithPowerThreeOrGreater() {
        Permanent naginata = addReadyNaginata(player1);
        Permanent creature = addCreatureReady(player1, new RavingOniSlave());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(naginata.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipRequiresTwoColorlessMana() {
        Permanent naginata = addReadyNaginata(player1);
        Permanent creature = addCreatureReady(player1, new RavingOniSlave());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(naginata.getAttachedTo()).isNull();
    }

    @Test
    void equipCanTargetLowPowerCreatureButDoesNotAttach() {
        Permanent naginata = addReadyNaginata(player1);
        Permanent creature = addCreatureReady(player1, new ArabaMothrider());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(naginata.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotTargetCreatureOpponentControls() {
        Permanent naginata = addReadyNaginata(player1);
        Permanent creature = addCreatureReady(player2, new KamiOfTheTendedGarden());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(naginata.getAttachedTo()).isNull();
    }

    @Test
    void equipOnlyActivatesAtSorcerySpeed() {
        Permanent naginata = addReadyNaginata(player1);
        Permanent creature = addCreatureReady(player1, new KamiOfTheTendedGarden());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(naginata.getAttachedTo()).isNull();
    }

    @Test
    @CardUsed(Hydrosurge.class)
    void becomesUnattachedWhenEquippedCreaturePowerDropsBelowThree() {
        Permanent creature = addCreatureReady(player1, new KamiOfTheTendedGarden());
        Permanent naginata = addReadyNaginata(player1);
        naginata.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Hydrosurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(naginata.getAttachedTo()).isNull();
    }

    private Permanent addReadyNaginata(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ONaginata());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
