package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DalekDrone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcesBaseballBat.class, DalekDrone.class, GrizzlyBears.class, KamahlPitFighter.class})
class AcesBaseballBatTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0 and first strike only while attacking")
    void equippedCreatureGetsBoostAndAttackOnlyFirstStrike() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent bat = addReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        creature.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An attacking equipped creature must be blocked by a Dalek if able")
    void requiresDalekBlockerIfAble() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        Permanent bat = addReady(player1, new AcesBaseballBat());
        bat.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        addReady(player2, new GrizzlyBears());
        Permanent dalek = addReady(player2, new DalekDrone());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("matching creature");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(dalek.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equip legendary creature costs {1}, while ordinary equip costs {3}")
    void supportsBothEquipAbilities() {
        Permanent bat = addReady(player1, new AcesBaseballBat());
        Permanent legendaryCreature = addReady(player1, new KamahlPitFighter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, bat), 0, null,
                legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(bat.getAttachedTo()).isEqualTo(legendaryCreature.getId());

        Permanent ordinaryCreature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, bat), 1, null,
                ordinaryCreature.getId());
        harness.passBothPriorities();

        assertThat(bat.getAttachedTo()).isEqualTo(ordinaryCreature.getId());
    }

    @Test
    @DisplayName("The legendary equip ability rejects a nonlegendary creature")
    void legendaryEquipRejectsNonlegendaryCreature() {
        Permanent bat = addReady(player1, new AcesBaseballBat());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, bat), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
