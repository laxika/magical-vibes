package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.PhyrexianFurnace;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.w.WindingCanyons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullRod.class, MindStone.class, PhyrexianFurnace.class, WindingCanyons.class, SongOfTheDryads.class})
class NullRodTest extends BaseCardTest {

    @Test
    void preventsArtifactManaAbilities() {
        addNullRod(player1);
        harness.addToBattlefield(player2, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void preventsNonManaArtifactAbilities() {
        addNullRod(player1);
        harness.addToBattlefield(player2, new PhyrexianFurnace());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void doesNotPreventLandManaAbilities() {
        addNullRod(player1);
        harness.addToBattlefield(player2, new WindingCanyons());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void doesNotPreventNonManaAbilitiesOfLands() {
        addNullRod(player1);
        harness.addToBattlefield(player2, new WindingCanyons());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void preventsOwnArtifactAbilities() {
        addNullRod(player1);
        harness.addToBattlefield(player1, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void removingNullRodReenablesArtifactAbilities() {
        Permanent nullRod = harness.addToBattlefieldAndReturn(player1, new NullRod());
        harness.addToBattlefield(player2, new MindStone());

        gd.playerBattlefields.get(player1.getId()).remove(nullRod);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tappedNullRodStillPreventsArtifactAbilities() {
        Permanent nullRod = harness.addToBattlefieldAndReturn(player1, new NullRod());
        nullRod.setTapped(true);
        harness.addToBattlefield(player2, new MindStone());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void doesNotStopAnArtifactAbilityAlreadyOnTheStack() {
        harness.addToBattlefield(player1, new MindStone());
        harness.setLibrary(player1, List.of(new WindingCanyons()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        addNullRod(player2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Winding Canyons");
        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingPrintedAbilitiesReenablesArtifactManaAbilities() {
        Permanent nullRod = harness.addToBattlefieldAndReturn(player1, new NullRod());
        harness.addToBattlefield(player2, new MindStone());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, nullRod.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void losingPrintedAbilitiesReenablesNonManaArtifactAbilities() {
        Permanent nullRod = harness.addToBattlefieldAndReturn(player1, new NullRod());
        harness.addToBattlefield(player2, new MindStone());
        harness.setLibrary(player2, List.of(new WindingCanyons()));
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, nullRod.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Winding Canyons");
        harness.assertInGraveyard(player2, "Mind Stone");
    }

    private void addNullRod(Player player) {
        harness.addToBattlefield(player, new NullRod());
    }
}
