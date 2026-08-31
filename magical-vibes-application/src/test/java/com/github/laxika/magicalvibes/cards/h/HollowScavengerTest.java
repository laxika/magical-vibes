package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BakeryRaid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HollowScavenger.class, BakeryRaid.class})
class HollowScavengerTest extends BaseCardTest {

    @Test
    void adventureCreatesFoodAndExilesTheCard() {
        HollowScavenger card = new HollowScavenger();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAndSacrificeFoodForTemporaryBoost() {
        HollowScavenger card = castAdventure();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        Permanent scavenger = findPermanent(player1, "Hollow Scavenger");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(4);
    }

    @Test
    void boostAbilityCanBeActivatedOnlyOnceEachTurn() {
        HollowScavenger card = castAdventure();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        Permanent scavenger = findPermanent(player1, "Hollow Scavenger");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, scavenger), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    private HollowScavenger castAdventure() {
        HollowScavenger card = new HollowScavenger();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        return card;
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
