package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArmoryOfIroas;
import com.github.laxika.magicalvibes.cards.a.AjaniMentorOfHeroes;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningDiadem.class, GoldenHind.class, ArmoryOfIroas.class,
        AjaniMentorOfHeroes.class, InvasionOfZendikar.class})
class LightningDiademTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and the ETB trigger deals 2 damage to a player")
    void boostsEnchantedCreatureAndDamagesPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setLife(player2, 20);
        castLightningDiadem(creature.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The ETB trigger can damage the enchanted creature")
    void damagesEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        castLightningDiadem(creature.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ETB damage trigger cannot target a noncreature artifact")
    void rejectsInvalidEtbTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ArmoryOfIroas());
        castLightningDiadem(creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The ETB damage trigger can target a battle")
    void damagesBattle() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        castLightningDiadem(creature.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ETB damage trigger can target its controller")
    void damagesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        harness.setLife(player1, 20);
        castLightningDiadem(creature.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The ETB damage trigger removes loyalty from a planeswalker")
    void damagesPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AjaniMentorOfHeroes());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        castLightningDiadem(creature.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB damage target can differ from the enchanted creature")
    void killsAnotherCreatureWithoutBoostingIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        castLightningDiadem(creature.getId());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Golden Hind");
        harness.assertInGraveyard(player2, "Golden Hind");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    private void castLightningDiadem(java.util.UUID enchantTargetId) {
        harness.setHand(player1, List.of(new LightningDiadem()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castEnchantment(player1, 0, enchantTargetId);
    }
}
