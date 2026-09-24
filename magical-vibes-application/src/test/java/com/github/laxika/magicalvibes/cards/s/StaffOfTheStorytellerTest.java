package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.cards.c.CuriousInquiry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StaffOfTheStoryteller.class, Forest.class, CallTheCavalry.class, GrizzlyBears.class, CuriousInquiry.class})
class StaffOfTheStorytellerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying Spirit and gets a story counter for it")
    void entersAndTracksCreatureTokenCreation() {
        harness.setHand(player1, List.of(new StaffOfTheStoryteller()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent staff = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof StaffOfTheStoryteller)
                .findFirst()
                .orElseThrow();
        assertThat(staff.getCounterCount(CounterType.STORY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> "Spirit".equals(permanent.getCard().getName())).hasSize(1);
    }

    @Test
    @DisplayName("Removes a story counter to draw a card")
    void removesStoryCounterAndDraws() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        staff.setCounterCount(CounterType.STORY, 1);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(staff.getCounterCount(CounterType.STORY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(staff.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not get a story counter for a noncreature token")
    void ignoresNoncreatureTokenCreation() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTheStoryteller());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent inquiry = harness.addToBattlefieldAndReturn(player1, new CuriousInquiry());
        inquiry.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(staff.getCounterCount(CounterType.STORY)).isZero();
    }
}
