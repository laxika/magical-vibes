package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HopefulEidolon;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmamentOfNyxTest extends BaseCardTest {

    private Permanent attachArmament(Permanent creature) {
        Permanent armament = new Permanent(new ArmamentOfNyx());
        armament.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(armament);
        return armament;
    }

    @Test
    @DisplayName("Enchantment creature enchanted with Armament of Nyx has double strike")
    void enchantmentCreatureGetsDoubleStrike() {
        Permanent eidolon = new Permanent(new HopefulEidolon());
        gd.playerBattlefields.get(player1.getId()).add(eidolon);

        attachArmament(eidolon);

        assertThat(gqs.hasKeyword(gd, eidolon, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Enchantment creature enchanted with Armament of Nyx deals double strike damage")
    void enchantmentCreatureDealsDoubleStrikeDamage() {
        harness.setLife(player2, 20);
        Permanent eidolon = new Permanent(new HopefulEidolon());
        eidolon.setSummoningSick(false);
        eidolon.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(eidolon);
        attachArmament(eidolon);
        int power = gqs.getEffectivePower(gd, eidolon);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20 - 2 * power);
    }

    @Test
    @DisplayName("Non-enchantment creature enchanted with Armament of Nyx deals no damage")
    void nonEnchantmentCreatureDealsNoDamage() {
        harness.setLife(player2, 20);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        attachArmament(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage to a non-enchantment creature enchanted with Armament of Nyx is not prevented")
    void damageToNonEnchantmentCreatureStillApplies() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        attachArmament(bears);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Armament of Nyx can target only a creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ArmamentOfNyx()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
