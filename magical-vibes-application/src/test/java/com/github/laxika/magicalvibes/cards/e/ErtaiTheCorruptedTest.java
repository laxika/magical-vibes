package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArcticMerfolk;
import com.github.laxika.magicalvibes.cards.w.WarpedDevotion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErtaiTheCorrupted.class, ArcticMerfolk.class, WarpedDevotion.class})
class ErtaiTheCorruptedTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell by sacrificing Ertai")
    void countersSpellBySacrificingItself() {
        addCreatureReady(player1, new ErtaiTheCorrupted());
        harness.addMana(player1, ManaColor.BLUE, 1);

        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, merfolk, "{1}{U}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ertai, the Corrupted");
        harness.assertInGraveyard(player2, "Arctic Merfolk");
        harness.assertNotOnBattlefield(player1, "Ertai, the Corrupted");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can sacrifice an enchantment instead of Ertai")
    void countersSpellBySacrificingEnchantment() {
        Permanent ertai = addCreatureReady(player1, new ErtaiTheCorrupted());
        Permanent devotion = harness.addToBattlefieldAndReturn(player1, new WarpedDevotion());
        harness.addMana(player1, ManaColor.BLUE, 1);

        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, merfolk, "{1}{U}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, merfolk.getId());
        harness.handlePermanentChosen(player1, devotion.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ertai).doesNotContain(devotion);
        assertThat(ertai.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Warped Devotion");
        harness.assertInGraveyard(player2, "Arctic Merfolk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a noncreature spell by sacrificing another creature")
    void countersNoncreatureSpellBySacrificingAnotherCreature() {
        Permanent ertai = addCreatureReady(player1, new ErtaiTheCorrupted());
        Permanent merfolk = addCreatureReady(player1, new ArcticMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        WarpedDevotion devotion = new WarpedDevotion();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, devotion, "{2}{B}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, devotion.getId());
        harness.handlePermanentChosen(player1, merfolk.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ertai).doesNotContain(merfolk);
        assertThat(ertai.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Arctic Merfolk");
        harness.assertInGraveyard(player2, "Warped Devotion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        addCreatureReady(player1, new ErtaiTheCorrupted());
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new ArcticMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent ertai = addCreatureReady(player1, new ErtaiTheCorrupted());
        ertai.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, merfolk, "{1}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new ErtaiTheCorrupted());
        harness.addMana(player1, ManaColor.BLUE, 1);

        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, merfolk, "{1}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
