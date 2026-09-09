package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KorBladewhirl;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicCaptain.class, KorBladewhirl.class, GrizzlyBears.class})
class AngelicCaptainTest extends BaseCardTest {

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Gets +1/+1 for each other attacking Ally")
    void boostScalesWithOtherAttackingAllies() {
        Permanent captain = addReady(new AngelicCaptain());
        addReady(new KorBladewhirl());
        addReady(new KorBladewhirl());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(2);
        assertThat(captain.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking alone gives no boost")
    void noBoostWhenAttackingAlone() {
        Permanent captain = addReady(new AngelicCaptain());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isZero();
        assertThat(captain.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts only other attacking Allies")
    void ignoresNonAlliesAndNonAttackingAllies() {
        Permanent captain = addReady(new AngelicCaptain());
        addReady(new KorBladewhirl());
        addReady(new KorBladewhirl());
        addReady(new GrizzlyBears());

        declareAttackers(List.of(0, 1, 3));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }
}
