package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.cards.i.ImpetuousSunchaser;
import com.github.laxika.magicalvibes.cards.w.WhipOfErebos;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MogisGodOfSlaughter.class, FelhideBrawler.class, ImpetuousSunchaser.class,
        MycosynthLattice.class, WhipOfErebos.class})
class MogisGodOfSlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Mogis is not a creature below seven combined black and red devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent mogis = addMogis();
        addBlackDevotion(4);

        assertThat(gqs.isCreature(gd, mogis)).isFalse();
        assertThat(gqs.isEnchantment(gd, mogis)).isTrue();
    }

    @Test
    @DisplayName("Mogis becomes a creature at seven combined black and red devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent mogis = addMogis();
        addBlackDevotion(4);
        harness.addToBattlefield(player1, new ImpetuousSunchaser());

        assertThat(gqs.isCreature(gd, mogis)).isTrue();
    }

    @Test
    @DisplayName("Opponent declines to sacrifice and takes 2 damage on their upkeep")
    void opponentDeclinesAndTakesDamage() {
        harness.addToBattlefield(player1, new MogisGodOfSlaughter());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent sacrifices a creature instead of taking damage")
    void opponentSacrificesCreature() {
        harness.addToBattlefield(player1, new MogisGodOfSlaughter());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FelhideBrawler());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new MogisGodOfSlaughter());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FelhideBrawler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FelhideBrawler());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(second.getId()));
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent may take damage even when they can sacrifice a creature")
    void opponentDeclinesWithCreatureAvailable() {
        addMogis();
        harness.addToBattlefield(player2, new FelhideBrawler());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Felhide Brawler");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mogis does not trigger on its controller's upkeep")
    void doesNotTriggerOnControllerUpkeep() {
        addMogis();

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Mogis stops being a creature when devotion falls below seven")
    void stopsBeingCreatureWhenDevotionFalls() {
        Permanent mogis = addMogis();
        addBlackDevotion(4);
        Permanent redPermanent = harness.addToBattlefieldAndReturn(player1, new ImpetuousSunchaser());
        assertThat(gqs.isCreature(gd, mogis)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(redPermanent);

        assertThat(gqs.isCreature(gd, mogis)).isFalse();
        assertThat(gqs.isEnchantment(gd, mogis)).isTrue();
    }

    @Test
    @DisplayName("Opponent's permanents do not contribute to Mogis's devotion")
    void opponentDevotionDoesNotAnimateMogis() {
        Permanent mogis = addMogis();
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new FelhideBrawler());
        }

        assertThat(gqs.isCreature(gd, mogis)).isFalse();
    }

    @Test
    @DisplayName("Mogis's upkeep ability survives its source leaving the battlefield")
    void triggerResolvesAfterMogisLeaves() {
        Permanent mogis = addMogis();
        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(mogis);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed({MycosynthLattice.class})
    @DisplayName("Below devotion threshold Mogis retains artifact type granted by an older Lattice")
    void retainsOtherCardTypesBelowDevotionThreshold() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent mogis = addMogis();

        assertThat(gqs.getEffectiveCardTypes(gd, mogis))
                .contains(CardType.ENCHANTMENT, CardType.ARTIFACT)
                .doesNotContain(CardType.CREATURE);
    }

    @Test
    @CardUsed({WhipOfErebos.class})
    @DisplayName("Mogis's upkeep damage gains life when Mogis has lifelink")
    void upkeepDamageAppliesLifelink() {
        Permanent mogis = addMogis();
        harness.addToBattlefield(player1, new WhipOfErebos());
        addBlackDevotion(3);
        assertThat(gqs.isCreature(gd, mogis)).isTrue();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    private Permanent addMogis() {
        return harness.addToBattlefieldAndReturn(player1, new MogisGodOfSlaughter());
    }

    private void addBlackDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new FelhideBrawler());
        }
    }
}
