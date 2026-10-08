package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BogRaiders;
import com.github.laxika.magicalvibes.cards.c.CrazedSkirge;
import com.github.laxika.magicalvibes.cards.d.Despondency;
import com.github.laxika.magicalvibes.cards.e.Expunge;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoiceOfGrace.class, Expunge.class, CrazedSkirge.class, BogRaiders.class,
        Despondency.class, Pacifism.class, Pestilence.class})
class VoiceOfGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent voice = addCreatureReady(player2, new VoiceOfGrace());

        harness.setHand(player1, List.of(new Expunge()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by a white Aura")
    void canBeTargetedByWhiteAura() {
        Permanent voice = addCreatureReady(player1, new VoiceOfGrace());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, voice.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot be enchanted by a black Aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent voice = addCreatureReady(player1, new VoiceOfGrace());

        harness.setHand(player1, List.of(new Despondency()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Black creature cannot block")
    void blackCreatureCannotBlock() {
        addCreatureReady(player1, new VoiceOfGrace());
        addCreatureReady(player2, new CrazedSkirge());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Survives combat damage from a black creature")
    void survivesCombatDamageFromBlackCreature() {
        addCreatureReady(player1, new BogRaiders());
        Permanent voice = addCreatureReady(player2, new VoiceOfGrace());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(voice.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(voice);
    }

    @Test
    @DisplayName("Prevents repeated noncombat damage from a black source")
    void preventsRepeatedBlackNoncombatDamage() {
        harness.addToBattlefield(player1, new Pestilence());
        Permanent voice = addCreatureReady(player2, new VoiceOfGrace());
        harness.addMana(player1, ManaColor.BLACK, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(voice.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Voice of Grace");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A white flying creature can block and deal lethal damage")
    void whiteFlyingCreatureCanBlockAndDealDamage() {
        addCreatureReady(player1, new VoiceOfGrace());
        addCreatureReady(player2, new VoiceOfGrace());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Voice of Grace");
        harness.assertNotOnBattlefield(player2, "Voice of Grace");
        harness.assertInGraveyard(player1, "Voice of Grace");
        harness.assertInGraveyard(player2, "Voice of Grace");
    }
}
