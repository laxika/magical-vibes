package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
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

@CardUsed({KangeesLieutenant.class, WindDrake.class, GrizzlyBears.class})
class KangeesLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("When Kangee's Lieutenant attacks, attacking creatures with flying get +1/+1")
    void attackingCreaturesWithFlyingGetBoost() {
        Permanent lieutenant = addCreatureReady(player1, new KangeesLieutenant());
        Permanent drake = addCreatureReady(player1, new WindDrake());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(lieutenant.getPowerModifier()).isEqualTo(1);
        assertThat(lieutenant.getToughnessModifier()).isEqualTo(1);
        assertThat(drake.getPowerModifier()).isEqualTo(1);
        assertThat(drake.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Encore creates a hasty token copy attacking the opponent")
    void encoreCreatesHastyAttackingTokenCopy() {
        harness.setGraveyard(player1, List.of(new KangeesLieutenant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }
}
