package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ATaleForTheAges.class, GrizzlyBears.class, Pacifism.class})
class ATaleForTheAgesTest extends BaseCardTest {

    private Permanent attachPacifism(Permanent creature, UUID controllerId) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controllerId).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creatures you control get +2/+2")
    void boostsEnchantedCreaturesYouControl() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new ATaleForTheAges()));
        Permanent enchantedBear = new Permanent(new GrizzlyBears());
        Permanent unenchantedBear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedBear);
        gd.playerBattlefields.get(player1.getId()).add(unenchantedBear);
        attachPacifism(enchantedBear, player1.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, unenchantedBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unenchantedBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature is boosted when enchanted by an Aura controlled by another player")
    void auraControllerDoesNotMatter() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new ATaleForTheAges()));
        Permanent enchantedBear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedBear);
        attachPacifism(enchantedBear, player2.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creatures controlled by an opponent are not boosted")
    void doesNotBoostOpponentCreatures() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new ATaleForTheAges()));
        Permanent enchantedBear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(enchantedBear);
        attachPacifism(enchantedBear, player2.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost ends when the creature is no longer enchanted")
    void boostEndsWhenAuraIsRemoved() {
        Permanent tale = new Permanent(new ATaleForTheAges());
        gd.playerBattlefields.get(player1.getId()).add(tale);
        Permanent enchantedBear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedBear);
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
        Permanent tale = new Permanent(new ATaleForTheAges());
        gd.playerBattlefields.get(player1.getId()).add(tale);
        Permanent enchantedBear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedBear);
        attachPacifism(enchantedBear, player1.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(tale);

        assertThat(gqs.getEffectivePower(gd, enchantedBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedBear)).isEqualTo(2);
    }
}
