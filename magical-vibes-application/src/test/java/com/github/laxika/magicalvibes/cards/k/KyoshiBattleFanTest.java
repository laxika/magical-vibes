package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyoshiBattleFan.class, GrizzlyBears.class})
class KyoshiBattleFanTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Kyoshi Battle Fan creates an Ally token, attaches to it, and boosts it")
    void enteringCreatesAndAttachesAlly() {
        harness.setHand(player1, List.of(new KyoshiBattleFan()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent fan = findPermanent(player1, "Kyoshi Battle Fan");
        Permanent ally = findPermanent(player1, "Ally");
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
        assertThat(fan.getAttachedTo()).isEqualTo(ally.getId());
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip {2} attaches Kyoshi Battle Fan to a creature you control")
    void equipAttachesToCreature() {
        Permanent fan = addReady(player1, new KyoshiBattleFan());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(fan.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
