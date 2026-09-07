package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyxLotus.class, ElvishArchdruid.class, LlanowarElves.class})
class NyxLotusTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new NyxLotus()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void addsManaEqualToChosenColorDevotion() {
        addReadyLotus(player1);
        harness.addToBattlefield(player1, new ElvishArchdruid());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new ElvishArchdruid());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void choosingAColorWithoutDevotionAddsNoMana() {
        addReadyLotus(player1);
        harness.addToBattlefield(player1, new ElvishArchdruid());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private Permanent addReadyLotus(com.github.laxika.magicalvibes.model.Player player) {
        NyxLotus card = new NyxLotus();
        Permanent lotus = new Permanent(card);
        lotus.setSummoningSick(false);
        lotus.untap();
        gd.playerBattlefields.get(player.getId()).add(lotus);
        return lotus;
    }
}
