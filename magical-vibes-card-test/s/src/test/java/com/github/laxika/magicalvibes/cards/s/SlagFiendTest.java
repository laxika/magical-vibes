package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlagFiend.class, Sickleslicer.class, Plains.class, PorcelainLegionnaire.class})
class SlagFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Slag Fiend puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new SlagFiend()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(SlagFiend.class);
    }

    @Test
    @DisplayName("Resolving Slag Fiend puts it on the battlefield when graveyard has artifacts")
    void resolvingPutsItOnBattlefield() {
        harness.setGraveyard(player1, createArtifactCards(2));
        harness.setHand(player1, List.of(new SlagFiend()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Slag Fiend");
    }

    @Test
    @DisplayName("Slag Fiend dies to state-based actions when no artifacts in any graveyard")
    void diesWhenNoArtifactsInGraveyards() {
        harness.setHand(player1, List.of(new SlagFiend()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA
        harness.assertNotOnBattlefield(player1, "Slag Fiend");
        harness.assertInGraveyard(player1, "Slag Fiend");
    }

    @Test
    @DisplayName("Slag Fiend is 0/0 with no artifact cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SlagFiend());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Slag Fiend P/T equals number of artifact cards in controller's graveyard")
    void ptEqualsArtifactCountInOwnGraveyard() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SlagFiend());
        harness.setGraveyard(player1, createArtifactCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Slag Fiend P/T counts artifact cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SlagFiend());
        harness.setGraveyard(player1, createArtifactCards(2));
        harness.setGraveyard(player2, createArtifactCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Slag Fiend only counts artifact cards, not non-artifact cards")
    void onlyCountsArtifactCards() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SlagFiend());

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createArtifactCards(2));
        graveyard.add(new Plains());
        graveyard.add(new SlagFiend());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Slag Fiend P/T updates when artifacts are added to graveyard")
    void ptUpdatesWhenArtifactsAddedToGraveyard() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SlagFiend());
        harness.setGraveyard(player1, createArtifactCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new Sickleslicer());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Slag Fiend P/T counts artifact creatures (they are artifacts)")
    void ptCountsArtifactCreatures() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SlagFiend());

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new PorcelainLegionnaire());
        graveyard.add(new Sickleslicer());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Slag Fiend P/T counts opponent's graveyard artifacts too")
    void ptCountsOpponentsGraveyard() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SlagFiend());
        harness.setGraveyard(player2, createArtifactCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    @Test
    @DisplayName("Slag Fiend shrinks when artifact cards leave either graveyard")
    void ptUpdatesWhenArtifactsLeaveGraveyards() {
        harness.setGraveyard(player1, createArtifactCards(2));
        harness.setGraveyard(player2, createArtifactCards(3));
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SlagFiend());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);

        harness.setGraveyard(player2, createArtifactCards(1));
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Slag Fiend defines its power and toughness in hand and graveyard")
    void characteristicAbilityWorksOutsideBattlefield() {
        SlagFiend fiend = new SlagFiend();
        harness.setHand(player1, List.of(fiend));
        harness.setGraveyard(player1, createArtifactCards(2));
        harness.setGraveyard(player2, createArtifactCards(1));

        assertThat(gqs.getEffectiveCardPower(gd, fiend)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, fiend)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(fiend));
        assertThat(gqs.getEffectiveCardPower(gd, fiend)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, fiend)).isEqualTo(1);
    }

    private List<Card> createArtifactCards(int count) {
        List<Card> artifacts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            artifacts.add(new Sickleslicer());
        }
        return artifacts;
    }
}
