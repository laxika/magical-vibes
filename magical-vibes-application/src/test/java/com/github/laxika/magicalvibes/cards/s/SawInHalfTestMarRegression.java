package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawInHalf.class, GrizzlyBears.class, FountainOfYouth.class})
class SawInHalfTestMarRegression extends BaseCardTest {

    @Test
    void destroysCreatureAndCreatesTwoHalfSizedCopies() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setPowerModifier(3);
        target.setToughnessModifier(1);

        castSawInHalf(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        List<Permanent> copies = findPermanents(player2, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).allSatisfy(copy -> {
            assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        });
    }

    @Test
    void doesNotCreateCopiesWhenRegenerationPreventsDestruction() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setRegenerationShield(1);

        castSawInHalf(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    void canOnlyTargetCreatures() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SawInHalf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Fountain of Youth")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSawInHalf(Permanent target) {
        harness.setHand(player1, List.of(new SawInHalf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
