package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AspectOfMongoose.class, Boomerang.class, Disenchant.class, GrizzlyBears.class})
class AspectOfMongooseTest extends BaseCardTest {

    @Test
    @DisplayName("Aspect of Mongoose gives the enchanted creature shroud")
    void enchantedCreatureHasShroud() {
        Permanent creature = addCreature();
        attachAura(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud prevents targeting the enchanted creature")
    void shroudPreventsTargetingEnchantedCreature() {
        Permanent creature = addCreature();
        attachAura(creature);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, creature.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Aspect of Mongoose returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandWhenDestroyed() {
        Permanent creature = addCreature();
        Permanent aura = attachAura(creature);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, aura.getId());
        resolveStackFully();

        harness.assertNotOnBattlefield(player1, "Aspect of Mongoose");
        harness.assertNotInGraveyard(player1, "Aspect of Mongoose");
        harness.assertInHand(player1, "Aspect of Mongoose");
    }

    private Permanent addCreature() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        return creature;
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new AspectOfMongoose());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void resolveStackFully() {
        for (int i = 0; i < 8 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
