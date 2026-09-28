package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoldForgedSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.HuntedByTheFamilyEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntedByTheFamily.class, GoldForgedSentinel.class, GrizzlyBears.class})
class HuntedByTheFamilyTest extends BaseCardTest {

    @Test
    void eachTargetControllerChoosesIndependently() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new GoldForgedSentinel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(artifactCreature.getId(), bears.getId()));

        PendingInteraction.ColorChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.options()).containsExactly(
                HuntedByTheFamilyEffect.HUMAN_OPTION, HuntedByTheFamilyEffect.COPY_OPTION);

        harness.handleListChoice(player2, HuntedByTheFamilyEffect.HUMAN_OPTION);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player2, HuntedByTheFamilyEffect.COPY_OPTION);

        assertThat(gqs.getEffectiveCardTypes(gd, artifactCreature)).containsExactly(CardType.CREATURE);
        assertThat(gqs.getEffectiveColors(gd, artifactCreature)).containsExactly(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifactCreature)).containsExactly(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, artifactCreature, com.github.laxika.magicalvibes.model.Keyword.FLYING))
                .isFalse();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    void cannotTargetACreatureYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntedByTheFamily()));
        addMana();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.castSorcery(player1, 0, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new HuntedByTheFamily()));
        addMana();
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
