package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addToBattlefield(player1, new RagingGoblin());

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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

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
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

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

    private Permanent addMogis() {
        return harness.addToBattlefieldAndReturn(player1, new MogisGodOfSlaughter());
    }

    private void addBlackDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }
}
