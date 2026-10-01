package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ATaleForTheAges.class, GrizzlyBears.class, Pacifism.class, Opalescence.class})
class ATaleForTheAgesTest extends BaseCardTest {

    private Permanent attachPacifism(Permanent creature, UUID controllerId) {
        Permanent aura = harness.addToBattlefieldAndReturn(
                controllerId.equals(player1.getId()) ? player1 : player2, new Pacifism());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creatures you control get +2/+2")
    void boostsEnchantedCreaturesYouControl() {
        harness.addToBattlefield(player1, new ATaleForTheAges());
        Permanent enchantedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unenchantedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachPacifism(enchantedBear, player1.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, unenchantedBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature is boosted when enchanted by an Aura controlled by another player")
    void auraControllerDoesNotMatter() {
        harness.addToBattlefield(player1, new ATaleForTheAges());
        Permanent enchantedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachPacifism(enchantedBear, player2.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creatures controlled by an opponent are not boosted")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new ATaleForTheAges());
        Permanent enchantedBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachPacifism(enchantedBear, player2.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost ends when the creature is no longer enchanted")
    void boostEndsWhenAuraIsRemoved() {
        harness.addToBattlefield(player1, new ATaleForTheAges());
        Permanent enchantedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = attachPacifism(enchantedBear, player1.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost ends when A Tale for the Ages leaves the battlefield")
    void boostEndsWhenTaleIsRemoved() {
        Permanent tale = harness.addToBattlefieldAndReturn(player1, new ATaleForTheAges());
        Permanent enchantedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachPacifism(enchantedBear, player1.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(tale);

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Auras do not multiply the bonus and one remaining Aura preserves it")
    void multipleAurasGiveOneBonus() {
        harness.addToBattlefield(player1, new ATaleForTheAges());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstAura = attachPacifism(bear, player1.getId());
        attachPacifism(bear, player2.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(firstAura);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple copies each boost an enchanted creature")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new ATaleForTheAges());
        harness.addToBattlefield(player1, new ATaleForTheAges());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachPacifism(bear, player1.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
    }

    @Test
    @DisplayName("An animated and enchanted A Tale for the Ages receives its own bonus")
    void animatedEnchantedTaleBoostsItself() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent tale = harness.addToBattlefieldAndReturn(player1, new ATaleForTheAges());
        attachPacifism(tale, player2.getId());

        assertThat(gqs.getEffectivePower(gd, tale)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tale)).isEqualTo(4);
    }
}
