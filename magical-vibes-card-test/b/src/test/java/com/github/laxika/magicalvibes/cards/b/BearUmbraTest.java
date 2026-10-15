package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BearUmbra.class, NestInvader.class, Forest.class})
class BearUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void boostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new NestInvader());
        attachBearUmbra(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Totem armor saves the enchanted creature and destroys Bear Umbra")
    void totemArmorSavesEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new NestInvader());
        attachBearUmbra(creature);
        creature.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Nest Invader");
        harness.assertInGraveyard(player1, "Bear Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking untaps all lands you control")
    void attackUntapsOwnLands() {
        Permanent creature = addCreatureReady(player1, new NestInvader());
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

    @Test
    @DisplayName("The enchanted creature's controller untaps their lands")
    void attackUntapsCreatureControllersLands() {
        Permanent creature = addCreatureReady(player2, new NestInvader());
        attachBearUmbra(creature);
        Permanent auraControllersForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creatureControllersForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        auraControllersForest.tap();
        creatureControllersForest.tap();

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(creatureControllersForest.isTapped()).isFalse();
        assertThat(auraControllersForest.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Umbra armor does not tap the creature and only protects it once")
    void armorProtectsOnlyOnceWithoutTapping() {
        Permanent creature = addCreatureReady(player1, new NestInvader());
        attachBearUmbra(creature);
        creature.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Nest Invader");
        harness.assertInGraveyard(player1, "Bear Umbra");
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getMarkedDamage()).isZero();
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Nest Invader");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private Permanent attachBearUmbra(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BearUmbra());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
