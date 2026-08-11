package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KithkeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Kithkin token when it is the only colored permanent")
    void createsOneTokenForOneColor() {
        harness.setHand(player1, List.of(new Kithkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countKithkinTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates one token for each distinct color among permanents controlled")
    void createsTokensForDistinctColors() {
        addPermanent(player1, new GrizzlyBears());
        addPermanent(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Kithkeeper()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countKithkinTokens(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapping three creatures gives Kithkeeper +3/+0 and flying until end of turn")
    void abilityBoostsAndGrantsFlying() {
        Permanent kithkeeper = addPermanent(player1, new Kithkeeper());
        addPermanent(player1, new GrizzlyBears());
        addPermanent(player1, new RagingGoblin());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kithkeeper)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, kithkeeper)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, kithkeeper, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream().filter(Permanent::isTapped)).hasSize(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kithkeeper)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, kithkeeper, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without three untapped creatures")
    void cannotActivateWithoutThreeCreatures() {
        addPermanent(player1, new Kithkeeper());
        addPermanent(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private long countKithkinTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .count();
    }
}
