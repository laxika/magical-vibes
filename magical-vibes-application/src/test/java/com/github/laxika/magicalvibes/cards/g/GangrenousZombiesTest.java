package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GangrenousZombiesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice deals 1 damage to each creature and each player without a snow Swamp")
    void dealsOneWithoutSnowSwamp() {
        addReadyZombies(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ownBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent oppBear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(ownBear.getMarkedDamage()).isEqualTo(1);
        assertThat(oppBear.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Gangrenous Zombies");
        harness.assertInGraveyard(player1, "Gangrenous Zombies");
    }

    @Test
    @DisplayName("Sacrifice deals 2 damage to each creature and each player with a snow Swamp")
    void dealsTwoWithSnowSwamp() {
        addReadyZombies(player1);
        addSnowSwamp(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Gangrenous Zombies");
    }

    @Test
    @DisplayName("Opponent snow Swamp does not upgrade damage")
    void ignoresOpponentSnowSwamp() {
        addReadyZombies(player1);
        addSnowSwamp(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent oppBear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(oppBear.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Activated ability requires tap — cannot activate when tapped")
    void activatedAbilityRequiresTap() {
        Permanent zombies = addReadyZombies(player1);
        zombies.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyZombies(Player player) {
        GangrenousZombies card = new GangrenousZombies();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSnowSwamp(Player player) {
        Permanent snowSwamp = new Permanent(new Swamp());
        TestCards.mutableCard(snowSwamp).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowSwamp);
        return snowSwamp;
    }
}
