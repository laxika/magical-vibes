package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hobble;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchonOfTheWildRose.class, GrizzlyBears.class, Hobble.class})
class ArchonOfTheWildRoseTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control enchanted by your Auras become 4/4 flyers")
    void boostsCreaturesEnchantedByYourAuras() {
        addCreatureReady(player1, new ArchonOfTheWildRose());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        attachAura(player1, ownCreature);
        attachAura(player2, opposingCreature);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An Aura controlled by an opponent does not enable the ability")
    void doesNotBoostCreatureEnchantedByOpponentsAura() {
        addCreatureReady(player1, new ArchonOfTheWildRose());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        attachAura(player2, ownCreature);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    private void attachAura(com.github.laxika.magicalvibes.model.Player controller, Permanent host) {
        Permanent aura = new Permanent(new Hobble());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
