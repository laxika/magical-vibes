package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({QuarumTrenchGnomes.class, Plains.class, Forest.class})
class QuarumTrenchGnomesTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the targeted Plains produce colorless mana")
    void makesTargetedPlainsProduceColorlessMana() {
        Permanent gnomes = addReadyPermanent(player1, new QuarumTrenchGnomes());
        Permanent targetedPlains = addReadyPermanent(player1, new Plains());
        Permanent otherPlains = addReadyPermanent(player1, new Plains());

        harness.activateAbility(player1, battlefieldIndex(gnomes), null, targetedPlains.getId());
        harness.passBothPriorities();

        harness.tapPermanent(player1, battlefieldIndex(targetedPlains));
        harness.tapPermanent(player1, battlefieldIndex(otherPlains));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Plains land")
    void cannotTargetNonPlainsLand() {
        Permanent gnomes = addReadyPermanent(player1, new QuarumTrenchGnomes());
        Permanent forest = addReadyPermanent(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(gnomes), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
