package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinCratermakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability deals 2 damage to target creature")
    void dealsTwoDamageToTargetCreature() {
        addReadyCratermaker(player1);
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Goblin Cratermaker");
    }

    @Test
    @DisplayName("Sacrifice ability destroys target colorless nonland permanent")
    void destroysTargetColorlessNonlandPermanent() {
        addReadyCratermaker(player1);
        addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Cratermaker");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Damage ability cannot target a noncreature permanent")
    void damageAbilityCannotTargetNoncreature() {
        addReadyCratermaker(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destruction ability cannot target a colored permanent")
    void destructionAbilityCannotTargetColoredPermanent() {
        addReadyCratermaker(player1);
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destruction ability cannot target a land")
    void destructionAbilityCannotTargetLand() {
        addReadyCratermaker(player1);
        Permanent target = new Permanent(new Island());
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCratermaker(Player player) {
        Permanent permanent = new Permanent(new GoblinCratermaker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = new Permanent(new FountainOfYouth());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
