package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DetectivesSatchel.class})
class DetectivesSatchelTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates twice when it enters the battlefield")
    void investigatesTwiceWhenItEnters() {
        castSatchel();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate before sacrificing an artifact")
    void cannotActivateBeforeSacrificingArtifact() {
        Permanent satchel = addReadySatchel();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrificed an artifact");

        assertThat(satchel.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Creates a flying Thopter after an artifact is sacrificed")
    void createsThopterAfterArtifactSacrifice() {
        castSatchel();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getEffectivePower()).isEqualTo(1);
        assertThat(thopter.getEffectiveToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColor()).isNull();
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(gqs.isArtifact(gd, thopter)).isTrue();
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    private Permanent addReadySatchel() {
        Permanent satchel = harness.addToBattlefieldAndReturn(player1, new DetectivesSatchel());
        satchel.setSummoningSick(false);
        return satchel;
    }

    private Permanent castSatchel() {
        harness.setHand(player1, List.of(new DetectivesSatchel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Detective's Satchel");
    }
}
