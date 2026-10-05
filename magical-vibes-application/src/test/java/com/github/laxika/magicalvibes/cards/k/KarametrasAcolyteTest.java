package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BowOfNylea;
import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarametrasAcolyte.class, ElvishArchdruid.class, HillGiant.class, LlanowarElves.class, BowOfNylea.class})
class KarametrasAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds green mana equal to green devotion")
    void tapAbilityAddsGreenManaEqualToDevotion() {
        Permanent acolyte = addCreatureReady(player1, new KarametrasAcolyte());
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new LlanowarElves());

        int acolyteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(acolyte);
        harness.activateAbility(player1, acolyteIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Devotion ignores permanents without green mana symbols and opponents' permanents")
    void tapAbilityIgnoresNonGreenAndOpponentsPermanents() {
        Permanent acolyte = addCreatureReady(player1, new KarametrasAcolyte());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new ElvishArchdruid());

        int acolyteIndex = gd.playerBattlefields.get(player1.getId()).indexOf(acolyte);
        harness.activateAbility(player1, acolyteIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability cannot be activated with summoning sickness")
    void tapAbilityBlockedBySummoningSickness() {
        harness.addToBattlefield(player1, new KarametrasAcolyte());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana ability resolves immediately and cannot be activated again while tapped")
    void manaAbilityResolvesImmediatelyAndPaysTapCost() {
        Permanent acolyte = addCreatureReady(player1, new KarametrasAcolyte());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(acolyte.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Devotion includes noncreature permanents but excludes cards in hand and graveyard")
    void devotionCountsNoncreaturePermanentsOnlyOnBattlefield() {
        addCreatureReady(player1, new KarametrasAcolyte());
        harness.addToBattlefield(player1, new BowOfNylea());
        harness.setHand(player1, java.util.List.of(new BowOfNylea()));
        harness.setGraveyard(player1, java.util.List.of(new BowOfNylea()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each activation uses current devotion including tapped permanents")
    void devotionIsReevaluatedForEachActivation() {
        Permanent acolyte = addCreatureReady(player1, new KarametrasAcolyte());
        Permanent otherAcolyte = addCreatureReady(player1, new KarametrasAcolyte());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(otherAcolyte);
        gd.playerGraveyards.get(player1.getId()).add(otherAcolyte.getCard());
        acolyte.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }
}