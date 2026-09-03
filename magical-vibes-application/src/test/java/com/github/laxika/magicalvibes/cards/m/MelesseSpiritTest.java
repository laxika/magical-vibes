package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AbyssalHunter;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GraveServitude;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SkulkingGhost;
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

@CardUsed({AbyssalHunter.class, DarkBanishing.class, GraveServitude.class, MelesseSpirit.class,
        MtendaHerder.class, Pacifism.class, SkulkingGhost.class})
class MelesseSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Black flying creature cannot block Melesse Spirit")
    void blackCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new MelesseSpirit());
        attacker.setAttacking(true);

        addCreatureReady(player2, new SkulkingGhost());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent spirit = addCreatureReady(player2, new MelesseSpirit());
        addCreatureReady(player2, new MtendaHerder());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, spirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Cannot be targeted by a black ability")
    void cannotBeTargetedByBlackAbility() {
        Permanent hunter = addCreatureReady(player1, new AbyssalHunter());
        Permanent spirit = addCreatureReady(player2, new MelesseSpirit());
        addCreatureReady(player2, new MtendaHerder());

        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(hunter), null, spirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Black creature cannot deal combat damage to Melesse Spirit")
    void blackCreatureDamageIsPrevented() {
        Permanent attacker = addCreatureReady(player1, new SkulkingGhost());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MelesseSpirit());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SkulkingGhost);
    }

    @Test
    @DisplayName("Can be targeted and enchanted by a white Aura")
    void canBeTargetedByWhiteAura() {
        Permanent spirit = addCreatureReady(player1, new MelesseSpirit());

        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Pacifism
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Cannot be enchanted by a black Aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent spirit = addCreatureReady(player1, new MelesseSpirit());

        harness.setHand(player1, List.of(new GraveServitude()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, spirit.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }
}
