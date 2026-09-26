package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HallOfTheBanditLord.class, HumbleBudoka.class, CounselOfTheSoratami.class})
class HallOfTheBanditLordTest extends BaseCardTest {

    @Test
    @DisplayName("Hall of the Bandit Lord enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new HallOfTheBanditLord()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Hall of the Bandit Lord").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping and paying 3 life adds {C}")
    void activatingAddsColorlessAndPaysLife() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfTheBanditLord());
        hall.untap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, 17);
        assertThat(gd.stack).isEmpty();
        assertThat(hall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature spell paid for with Hall's mana gains haste")
    void creatureCastWithHallManaGainsHaste() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfTheBanditLord());
        hall.untap();
        harness.activateAbility(player1, 0, null, null);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new HumbleBudoka()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Humble Budoka").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A creature spell paid for with ordinary mana does not gain haste")
    void creatureCastWithOrdinaryManaHasNoHaste() {
        harness.castFromHand(player1, new HumbleBudoka(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Humble Budoka").hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Mana spent on a noncreature spell does not grant haste to a later creature spell")
    void manaSpentOnNoncreatureSpellDoesNotGrantHasteLater() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfTheBanditLord());
        hall.untap();
        harness.setLibrary(player1, List.of(new HumbleBudoka(), new HumbleBudoka()));

        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new HumbleBudoka()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Humble Budoka").hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste granted by Hall lasts until end of turn")
    void hasteExpiresAtEndOfTurn() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new HallOfTheBanditLord());
        hall.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new HumbleBudoka()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent budoka = findPermanent(player1, "Humble Budoka");
        assertThat(budoka.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(budoka.hasKeyword(Keyword.HASTE)).isFalse();
    }
}
