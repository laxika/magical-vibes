package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AyliEternalPilgrim.class, GrizzlyBears.class, Forest.class})
class AyliEternalPilgrimTest extends BaseCardTest {

    @Test
    void gainsLifeEqualToSacrificedCreatureToughness() {
        Permanent ayli = addAyliReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, ayli), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotSacrificeAyliItself() {
        Permanent ayli = addAyliReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ayli), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesTargetNonlandPermanentAtThirtyLife() {
        Permanent ayli = addAyliReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 30);
        addExileAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void cannotActivateExileAbilityBelowLifeThreshold() {
        Permanent ayli = addAyliReady(player1);
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 29);
        addExileAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(victim);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void cannotTargetLandWithExileAbility() {
        Permanent ayli = addAyliReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new Forest());
        harness.setLife(player1, 30);
        addExileAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ayli), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAyliReady(Player player) {
        return addCreatureReady(player, new AyliEternalPilgrim());
    }

    private void addExileAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
