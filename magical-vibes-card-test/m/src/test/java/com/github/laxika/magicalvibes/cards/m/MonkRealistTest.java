package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonkRealist.class, AngelicChorus.class, ArgothianSwine.class, Disenchant.class})
class MonkRealistTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new MonkRealist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Monk Realist");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new ArgothianSwine());
        harness.setHand(player1, List.of(new MonkRealist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Argothian Swine");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entersWithoutEtbTriggerWhenNoEnchantmentExists() {
        harness.setHand(player1, List.of(new MonkRealist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Monk Realist");
    }

    @Test
    void destroysYourOwnEnchantmentWhenItIsTheOnlyEnchantment() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.setHand(player1, List.of(new MonkRealist()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Angelic Chorus"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Monk Realist");
        harness.assertOnBattlefield(player1, "Angelic Chorus");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Monk Realist");
    }

    @Test
    void doesNotDestroyAnotherEnchantmentWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.setHand(player1, List.of(new MonkRealist()));
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Monk Realist");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Angelic Chorus");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof AngelicChorus).hasSize(1);
    }
}
