package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.k.KenzoTheHardhearted;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BushiTenderfoot.class, KenzoTheHardhearted.class, KamiOfOldStone.class, Befoul.class})
class BushiTenderfootTest extends BaseCardTest {

    @Test
    @DisplayName("Flips after a creature dealt damage by it dies")
    void flipsAfterDamagedCreatureDies() {
        Permanent bushi = flipBushi();

        assertThat(bushi.isTransformed()).isTrue();
        harness.assertInGraveyard(player2, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Kenzo deals both first-strike and regular combat damage")
    void kenzoDealsDoubleStrikeDamage() {
        addCreatureReady(player1, new KenzoTheHardhearted());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Kenzo gets +2/+2 when it becomes blocked")
    void kenzoGetsBushidoBonusWhenBlocked() {
        Permanent bushi = flipBushi();
        bushi.untap();
        bushi.setAttacking(true);

        addCreatureReady(player2, new KamiOfOldStone());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();

        assertThat(bushi.getPowerModifier()).isEqualTo(2);
        assertThat(bushi.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kenzo gets +2/+2 when it blocks")
    void kenzoGetsBushidoBonusWhenItBlocks() {
        Permanent bushi = flipBushi();
        bushi.untap();
        bushi.setAttacking(false);

        addCreatureReady(player2, new KamiOfOldStone());
        Permanent attacker = findPermanent(player2, "Kami of Old Stone");
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();

        assertThat(bushi.getPowerModifier()).isEqualTo(2);
        assertThat(bushi.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not flip when an undamaged creature dies")
    void doesNotFlipWhenUndamagedCreatureDies() {
        Permanent bushi = addCreatureReady(player1, new BushiTenderfoot());
        Permanent victim = addCreatureReady(player2, new KamiOfOldStone());

        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, victim.getId());

        assertThat(bushi.isTransformed()).isFalse();
    }

    private Permanent flipBushi() {
        Permanent bushi = addCreatureReady(player1, new BushiTenderfoot());

        KamiOfOldStone blockerCard = new KamiOfOldStone();
        blockerCard.setPower(0);
        blockerCard.setToughness(1);
        addCreatureReady(player2, blockerCard);

        bushi.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();
        return bushi;
    }
}
