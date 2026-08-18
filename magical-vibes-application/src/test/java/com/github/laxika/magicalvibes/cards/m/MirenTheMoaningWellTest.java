package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MirenTheMoaningWellTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tappingForManaAddsColorless() {
        Permanent miren = addReadyMiren(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(miren.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a creature gains life equal to its toughness")
    void sacrificingCreatureGainsLifeEqualToToughness() {
        Permanent miren = addReadyMiren(player1);
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, spider.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(miren.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot activate the life-gain ability without a creature to sacrifice")
    void cannotActivateLifeGainAbilityWithoutCreature() {
        addReadyMiren(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");
    }

    private Permanent addReadyMiren(Player player) {
        Permanent permanent = new Permanent(new MirenTheMoaningWell());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static final class GiantSpider extends Card {

        private GiantSpider() {
            setName("Giant Spider");
            setType(CardType.CREATURE);
            setManaCost("{3}{G}");
            setColor(CardColor.GREEN);
            setPower(2);
            setToughness(4);
        }
    }
}
