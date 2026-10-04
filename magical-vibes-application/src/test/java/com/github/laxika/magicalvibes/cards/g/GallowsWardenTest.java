package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.v.VoicelessSpirit;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GallowsWarden.class, VoicelessSpirit.class, WalkingCorpse.class})
class GallowsWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Other Spirit creatures you control get +0/+1")
    void buffsOtherSpirits() {
        harness.addToBattlefield(player1, new GallowsWarden());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new VoicelessSpirit());

        // Voiceless Spirit is 2/1 base + 0/1 from Gallows Warden = 2/2
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gallows Warden does not buff itself")
    void doesNotBuffItself() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GallowsWarden());

        // Base 3/3, no self-buff
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Spirit creatures")
    void doesNotBuffNonSpirits() {
        harness.addToBattlefield(player1, new GallowsWarden());
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Spirit creatures")
    void doesNotBuffOpponentSpirits() {
        harness.addToBattlefield(player1, new GallowsWarden());
        Permanent opponentSpirit = harness.addToBattlefieldAndReturn(player2, new VoicelessSpirit());

        // Voiceless Spirit is 2/1 base, no buff from opponent's Gallows Warden
        assertThat(gqs.getEffectivePower(gd, opponentSpirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSpirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Gallows Wardens buff each other with +0/+1")
    void twoWardensBuffEachOther() {
        harness.addToBattlefield(player1, new GallowsWarden());
        harness.addToBattlefield(player1, new GallowsWarden());

        List<Permanent> wardens = findPermanents(player1, "Gallows Warden");

        assertThat(wardens).hasSize(2);
        for (Permanent warden : wardens) {
            // 3/3 base + 0/1 from the other Warden = 3/4
            assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Bonus is removed when Gallows Warden leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new GallowsWarden());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new VoicelessSpirit());

        // Buffed: 2/1 + 0/1 = 2/2
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);

        // Remove Gallows Warden
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Gallows Warden"));

        // Back to base 2/1
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonuses from multiple Gallows Wardens stack on another Spirit")
    void bonusesStackOnOtherSpirit() {
        harness.addToBattlefield(player1, new GallowsWarden());
        harness.addToBattlefield(player1, new GallowsWarden());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new VoicelessSpirit());

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Spirit entering after Gallows Warden immediately receives the bonus")
    void buffsSpiritEnteringLater() {
        harness.addToBattlefield(player1, new GallowsWarden());
        harness.castFromHand(player1, new VoicelessSpirit(), "{2}{W}");
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Voiceless Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }
}
