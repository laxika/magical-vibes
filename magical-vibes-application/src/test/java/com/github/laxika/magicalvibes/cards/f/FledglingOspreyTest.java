package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PatternOfRebirth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FledglingOsprey.class, PatternOfRebirth.class})
class FledglingOspreyTest extends BaseCardTest {

    @Test
    @DisplayName("Fledgling Osprey does not have flying while unenchanted")
    void doesNotHaveFlyingWhileUnenchanted() {
        Permanent osprey = addCreatureReady(player1, new FledglingOsprey());

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Fledgling Osprey has flying while enchanted")
    void hasFlyingWhileEnchanted() {
        Permanent osprey = addCreatureReady(player1, new FledglingOsprey());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PatternOfRebirth());
        aura.setAttachedTo(osprey.getId());

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Fledgling Osprey loses flying when the Aura leaves")
    void losesFlyingWhenAuraLeaves() {
        Permanent osprey = addCreatureReady(player1, new FledglingOsprey());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PatternOfRebirth());
        aura.setAttachedTo(osprey.getId());

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An Aura attached to another creature does not grant Fledgling Osprey flying")
    void auraAttachedToAnotherCreatureDoesNotGrantFlying() {
        Permanent osprey = addCreatureReady(player1, new FledglingOsprey());
        Permanent otherCreature = addCreatureReady(player1, new FledglingOsprey());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PatternOfRebirth());
        aura.setAttachedTo(otherCreature.getId());

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isTrue();
    }
}
