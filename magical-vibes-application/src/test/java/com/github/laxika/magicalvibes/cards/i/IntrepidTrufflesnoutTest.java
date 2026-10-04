package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoHogWild;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrepidTrufflesnout.class, GoHogWild.class, GrizzlyBears.class})
class IntrepidTrufflesnoutTest extends BaseCardTest {

    @Test
    void adventureBoostsTargetCreatureAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        IntrepidTrufflesnout card = new IntrepidTrufflesnout();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCanTargetOnlyCreature() {
        IntrepidTrufflesnout card = new IntrepidTrufflesnout();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCreatesFoodWhenItAttacksAlone() {
        Permanent trufflesnout = addCreatureReady(player1, new IntrepidTrufflesnout());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(trufflesnout.isAttacking()).isTrue();
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void creatureFaceDoesNotCreateFoodWhenItAttacksWithAnotherCreature() {
        addCreatureReady(player1, new IntrepidTrufflesnout());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Food"));
    }
}
