package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlaiveOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Glaive of the Guildpact grants vigilance and menace")
    void equippingGrantsKeywords() {
        Permanent glaive = addGlaiveReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(glaive.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0 for each Gate its controller controls")
    void equippedCreatureGetsGateBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glaive = addGlaiveReady(player1);
        glaive.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent-controlled Gates do not increase Glaive of the Guildpact's bonus")
    void opponentGatesDoNotCount() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glaive = addGlaiveReady(player1);
        glaive.setAttachedTo(creature.getId());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    private Permanent addGlaiveReady(Player player) {
        Permanent perm = new Permanent(new GlaiveOfTheGuildpact());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
