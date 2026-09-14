package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfWrath.class, FountainOfYouth.class, GrizzlyBears.class})
class GiftOfWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an enchanted creature and grants menace")
    void boostsEnchantedCreatureAndGrantsMenace() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGiftOfWrath(bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not boost an enchanted noncreature artifact")
    void doesNotBoostNoncreatureArtifact() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        castGiftOfWrath(fountain.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fountain)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, fountain)).isZero();
        assertThat(gqs.hasKeyword(gd, fountain, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Creates a red 2/2 Spirit token with menace when it leaves")
    void createsSpiritTokenWhenLeaving() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new GiftOfWrath());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> spirits = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .toList();
        assertThat(spirits).hasSize(1);
        assertThat(spirits.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(spirits.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(spirits.getFirst().getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED);
        assertThat(spirits.getFirst().getCard().getKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature nonartifact permanent")
    void rejectsInvalidTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new GiftOfWrath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    private void castGiftOfWrath(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new GiftOfWrath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, targetId);
    }
}
