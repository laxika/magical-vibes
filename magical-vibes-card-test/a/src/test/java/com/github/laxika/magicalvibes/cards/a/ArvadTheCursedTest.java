package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArvadTheCursed.class, AdelizTheCinderWind.class, PrimordialWurm.class, DeepFreeze.class})
class ArvadTheCursedTest extends BaseCardTest {

    @Test
    @DisplayName("Other legendary creatures you control get +2/+2")
    void buffsOtherLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        Permanent adeliz = harness.addToBattlefieldAndReturn(player1, new AdelizTheCinderWind());
        // Adeliz is 2/2 base + 2/2 from Arvad = 4/4
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(4);
    }

    @Test
    @DisplayName("Arvad the Cursed does not buff itself")
    void doesNotBuffItself() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        // 3/3 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-legendary creatures")
    void doesNotBuffNonLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not buff opponent's legendary creatures")
    void doesNotBuffOpponentLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        Permanent opponentAdeliz = harness.addToBattlefieldAndReturn(player2, new AdelizTheCinderWind());
        // Adeliz is 2/2 base, no buff from opponent's Arvad
        assertThat(gqs.getEffectivePower(gd, opponentAdeliz)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentAdeliz)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Arvad the Cursed leaves the battlefield")
    void bonusRemovedWhenArvadLeaves() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        Permanent adeliz = harness.addToBattlefieldAndReturn(player1, new AdelizTheCinderWind());
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Arvad the Cursed"));

        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(2);
    }

    @Test
    @DisplayName("Before state-based actions, two Arvads buff each other and stack bonuses")
    void twoArvadsStackBonuses() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new ArvadTheCursed());
        Permanent adeliz = harness.addToBattlefieldAndReturn(player1, new AdelizTheCinderWind());
        // 2/2 base + 2/2 from each Arvad = 6/6
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(6);

        // Each Arvad buffs the other (both are Legendary)
        List<Permanent> arvads = findPermanents(player1, "Arvad the Cursed");
        assertThat(arvads).hasSize(2);
        for (Permanent arvad : arvads) {
            // 3/3 base + 2/2 from the other Arvad = 5/5
            assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("Arvad gains life and kills a larger blocker through deathtouch")
    void deathtouchAndLifelinkApplyEvenWhenArvadDies() {
        addCreatureReady(player1, new ArvadTheCursed());
        addCreatureReady(player2, new PrimordialWurm());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Arvad the Cursed");
        harness.assertInGraveyard(player2, "Primordial Wurm");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Arvad's boost does not grant lifelink to other creatures")
    void boostedCreatureDoesNotGainLifelink() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        addCreatureReady(player1, new AdelizTheCinderWind());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Losing all abilities removes Arvad's bonus")
    void bonusRemovedWhenArvadLosesAbilities() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent adeliz = harness.addToBattlefieldAndReturn(player1, new AdelizTheCinderWind());
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(4);

        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, arvad.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(2);
    }

    @Test
    @DisplayName("An enchanted legendary creature still receives the bonus after losing its abilities")
    void legendaryCreatureStillGetsBonusAfterLosingAbilities() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        Permanent adeliz = harness.addToBattlefieldAndReturn(player1, new AdelizTheCinderWind());

        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, adeliz.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(6);
    }
}
