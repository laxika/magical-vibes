package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.h.HorizonSpellbomb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChromeSteed.class, HorizonSpellbomb.class, AccordersShield.class})
class ChromeSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Chrome Steed puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new ChromeSteed()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(ChromeSteed.class);
    }

    @Test
    @DisplayName("Base 2/2 when only Chrome Steed on battlefield (1 artifact)")
    void noMetalcraftWithOnlySelf() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new ChromeSteed());

        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);
    }

    @Test
    @DisplayName("Base 2/2 with Chrome Steed plus one other artifact (2 total)")
    void noMetalcraftWithTwoArtifacts() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new ChromeSteed());
        harness.addToBattlefield(player1, new HorizonSpellbomb());

        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 (becomes 4/4) with Chrome Steed plus two other artifacts (3 total)")
    void metalcraftWithThreeArtifacts() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new ChromeSteed());
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new ChromeSteed());
        harness.addToBattlefield(player1, new HorizonSpellbomb());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(4);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Horizon Spellbomb"));
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new ChromeSteed());
        harness.addToBattlefield(player2, new HorizonSpellbomb());
        harness.addToBattlefield(player2, new AccordersShield());

        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);
    }

    @Test
    @DisplayName("The third artifact enables metalcraft only after resolving")
    void gainsMetalcraftWhenThirdArtifactResolves() {
        Permanent steed = harness.addToBattlefieldAndReturn(player1, new ChromeSteed());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.setHand(player1, List.of(new ChromeSteed()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof ChromeSteed)
                .allSatisfy(permanent -> {
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
                });
    }
}
