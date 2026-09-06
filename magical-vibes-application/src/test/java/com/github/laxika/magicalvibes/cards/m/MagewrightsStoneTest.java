package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.s.SeekerOfSkybreak;
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

@CardUsed({MagewrightsStone.class, SeekerOfSkybreak.class, JungleDelver.class})
class MagewrightsStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a target creature with a tap ability")
    void untapsCreatureWithTapAbility() {
        Permanent stone = addReadyPermanent(player1, new MagewrightsStone());
        Permanent seeker = addReadyPermanent(player1, new SeekerOfSkybreak());
        seeker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, seeker.getId());
        harness.passBothPriorities();

        assertThat(seeker.isTapped()).isFalse();
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature without a tap ability")
    void cannotTargetCreatureWithoutTapAbility() {
        addReadyPermanent(player1, new MagewrightsStone());
        Permanent delver = addReadyPermanent(player1, new JungleDelver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, delver.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
