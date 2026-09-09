package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarvinMurderousMimic.class, DrudgeSkeletons.class, LlanowarElves.class})
class MarvinMurderousMimicTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated abilities from differently named creatures you control")
    void gainsOwnCreatureAbility() {
        Permanent marvin = addReady(player1, new MarvinMurderousMimic());
        addReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(marvin.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains a creature's tap-for-mana ability")
    void gainsOwnCreatureManaAbility() {
        addReady(player1, new MarvinMurderousMimic());
        addReady(player1, new LlanowarElves());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain activated abilities from creatures an opponent controls")
    void ignoresOpponentsCreatures() {
        addReady(player1, new MarvinMurderousMimic());
        addReady(player2, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
