package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.h.HashepOasis;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SidewinderNaga.class, HashepOasis.class, FrilledSandwalla.class})
class SidewinderNagaTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/2 with no trample when no Desert")
    void baseStatsWithoutDesert() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample while you control a Desert")
    void boostAndTrampleWithDesertOnBattlefield() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        harness.addToBattlefield(player1, new HashepOasis());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+0 and trample while a Desert card is in your graveyard")
    void boostAndTrampleWithDesertInGraveyard() {
        harness.setGraveyard(player1, List.of(new HashepOasis()));
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Battlefield and graveyard Deserts do not stack the boost")
    void bothDesertsDoNotStack() {
        harness.setGraveyard(player1, List.of(new HashepOasis()));
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        harness.addToBattlefield(player1, new HashepOasis());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Desert does not grant the boost or trample")
    void opponentDesertDoesNotCount() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        harness.addToBattlefield(player2, new HashepOasis());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's graveyard Desert does not grant the boost or trample")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new HashepOasis()));
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses boost and trample when its only Desert leaves the battlefield")
    void losesWhenDesertLeaves() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        harness.addToBattlefield(player1, new HashepOasis());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Hashep Oasis"));

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A non-Desert permanent does not grant the boost or trample")
    void nonDesertDoesNotCount() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        harness.addToBattlefield(player1, new FrilledSandwalla());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses boost and trample when the only graveyard Desert is removed")
    void losesWhenGraveyardDesertIsRemoved() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        harness.setGraveyard(player1, List.of(new HashepOasis()));

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Keeps boost and trample when its Desert moves from battlefield to graveyard")
    void keepsBonusWhenDesertMovesToGraveyard() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        HashepOasis desertCard = new HashepOasis();
        Permanent desert = harness.addToBattlefieldAndReturn(player1, desertCard);

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(desert);
        harness.setGraveyard(player1, List.of(desertCard));

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Deserts in hand and exile do not grant the bonus")
    void desertsInOtherZonesDoNotCount() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        harness.setHand(player1, List.of(new HashepOasis()));
        harness.setExile(player1, List.of(new HashepOasis()));

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, naga)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A non-Desert graveyard card does not grant the bonus")
    void nonDesertInGraveyardDoesNotCount() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        harness.setGraveyard(player1, List.of(new FrilledSandwalla()));

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The Desert bonus affects only Sidewinder Naga")
    void bonusDoesNotAffectOtherCreatures() {
        Permanent naga = harness.addToBattlefieldAndReturn(player1, new SidewinderNaga());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FrilledSandwalla());
        harness.addToBattlefield(player1, new HashepOasis());

        assertThat(gqs.getEffectivePower(gd, naga)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, naga, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }
}
