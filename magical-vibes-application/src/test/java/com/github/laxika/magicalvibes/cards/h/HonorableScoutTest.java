package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.d.DralnusCrusade;
import com.github.laxika.magicalvibes.cards.l.LavaZombie;
import com.github.laxika.magicalvibes.cards.m.MaggotCarrier;
import com.github.laxika.magicalvibes.cards.m.MireKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonorableScout.class, MaggotCarrier.class, MireKavu.class, AncientSpider.class,
        DralnusCrusade.class, LavaZombie.class})
class HonorableScoutTest extends BaseCardTest {

    private void castHonorableScout() {
        harness.setHand(player1, List.of(new HonorableScout()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Gains 2 life for each black and/or red creature the target opponent controls")
    void gainsLifePerBlackAndRedCreature() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new MaggotCarrier());
        harness.addToBattlefield(player2, new MireKavu());

        castHonorableScout();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A creature that is both black and red counts once")
    void countsMulticoloredCreatureOnce() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new LavaZombie());

        castHonorableScout();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Creatures that are neither black nor red are ignored")
    void ignoresOtherColors() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new AncientSpider());

        castHonorableScout();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Noncreature permanents are ignored")
    void ignoresNoncreaturePermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new DralnusCrusade());

        castHonorableScout();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only the target opponent's creatures count")
    void ignoresCastersCreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new MaggotCarrier());
        harness.addToBattlefield(player2, new MireKavu());

        castHonorableScout();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new HonorableScout()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
