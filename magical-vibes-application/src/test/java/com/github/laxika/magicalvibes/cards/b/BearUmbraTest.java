package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BearUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void boostsEnchantedCreature() {
        Permanent creature = addReadyCreature(player1);
        attachBearUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Totem armor saves the enchanted creature and destroys Bear Umbra")
    void totemArmorSavesEnchantedCreature() {
        Permanent creature = addReadyCreature(player1);
        attachBearUmbra(creature);
        creature.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bear Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking untaps all lands you control")
    void attackUntapsOwnLands() {
        Permanent creature = addReadyCreature(player1);
        attachBearUmbra(creature);
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownForest.tap();
        opponentForest.tap();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(ownForest.isTapped()).isFalse();
        assertThat(opponentForest.isTapped()).isTrue();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent attachBearUmbra(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BearUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
