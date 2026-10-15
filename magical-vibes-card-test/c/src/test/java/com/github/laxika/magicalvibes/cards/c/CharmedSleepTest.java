package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.s.SpareDagger;
import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharmedSleep.class, HillGiantHerdgorger.class, SpareDagger.class, YouComeToARiver.class})
class CharmedSleepTest extends BaseCardTest {

    @Test
    void resolvingTapsAndEnchantsTargetCreature() {
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());

        harness.setHand(player1, List.of(new CharmedSleep()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Charmed Sleep")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    void enchantedCreatureDoesNotUntapDuringItsControllerUntapStep() {
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        creature.tap();

        attachCharmedSleep(creature);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void creatureUntapsAfterCharmedSleepIsRemoved() {
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        creature.tap();

        Permanent aura = attachCharmedSleep(creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new SpareDagger());
        harness.setHand(player1, List.of(new CharmedSleep()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        Permanent artifact = findPermanent(player1, "Spare Dagger");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachCharmedSleep(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CharmedSleep());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    void enterTriggerStillTapsCreatureAfterAuraIsReturnedToHand() {
        Permanent creature = addCreatureReady(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new CharmedSleep(), new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        Permanent aura = findPermanent(player1, "Charmed Sleep");

        harness.castInstant(player1, 0, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Charmed Sleep");
        harness.assertNotOnBattlefield(player1, "Charmed Sleep");
        assertThat(creature.isTapped()).isFalse();

        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void onlyEnchantedCreatureStaysTappedDuringUntapStep() {
        Permanent enchanted = addCreatureReady(player2, new HillGiantHerdgorger());
        Permanent other = addCreatureReady(player2, new HillGiantHerdgorger());
        enchanted.tap();
        other.tap();
        attachCharmedSleep(enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }
}
