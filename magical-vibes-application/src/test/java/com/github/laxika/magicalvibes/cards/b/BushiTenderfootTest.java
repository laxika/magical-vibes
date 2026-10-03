package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AshmouthBlade;
import com.github.laxika.magicalvibes.cards.i.IndomitableWill;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.n.NeglectedHeirloom;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.k.KenzoTheHardhearted;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BushiTenderfoot.class, KenzoTheHardhearted.class, KamiOfOldStone.class, Befoul.class,
        IndomitableWill.class, IsamaruHoundOfKonda.class, BlessedBreath.class,
        NeglectedHeirloom.class, AshmouthBlade.class})
class BushiTenderfootTest extends BaseCardTest {

    @Test
    @DisplayName("Flips after a creature dealt damage by it dies")
    void flipsAfterDamagedCreatureDies() {
        Permanent bushi = flipBushi();

        assertThat(bushi.isTransformed()).isTrue();
        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
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

    @Test
    @DisplayName("Flips when a creature it damaged dies later in the same turn")
    void flipsAfterLaterDeath() {
        Permanent bushi = addCreatureReady(player1, new BushiTenderfoot());
        harness.setHand(player1, List.of(new IndomitableWill()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bushi.getId());
        Permanent victim = addCreatureReady(player2, new KamiOfOldStone());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();
        assertThat(bushi.isTransformed()).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, victim.getId());
        resolveAllTriggers();

        assertThat(bushi.isTransformed()).isTrue();
        harness.assertInGraveyard(player2, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Cannot survive lethal damage by flipping after simultaneous deaths")
    void doesNotFlipToSurviveLethalDamage() {
        addCreatureReady(player1, new BushiTenderfoot());
        addCreatureReady(player2, new BushiTenderfoot());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bushi Tenderfoot");
        harness.assertNotOnBattlefield(player1, "Kenzo the Hardhearted");
        harness.assertInGraveyard(player1, "Bushi Tenderfoot");
        harness.assertInGraveyard(player2, "Bushi Tenderfoot");
    }

    @Test
    @CardUsed({NeglectedHeirloom.class, AshmouthBlade.class})
    @DisplayName("Flipping Bushi does not trigger equipped-creature transform abilities")
    void flippingDoesNotTransformHeirloom() {
        Permanent bushi = addCreatureReady(player1, new BushiTenderfoot());
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new NeglectedHeirloom());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, bushi.getId());
        resolveAllTriggers();
        addCreatureReady(player2, new BushiTenderfoot());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(bushi.isTransformed()).isTrue();
        assertThat(heirloom.isTransformed()).isFalse();
        assertThat(heirloom.getAttachedTo()).isEqualTo(bushi.getId());
    }

    @Test
    @DisplayName("Kenzo's combat damage is prevented by protection from white")
    void flippedBushiRemainsWhiteForProtection() {
        Permanent bushi = flipBushi();
        bushi.untap();
        Permanent blocker = addCreatureReady(player2, new KamiOfOldStone());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BlessedBreath()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, blocker.getId());
        harness.handleListChoice(player2, "WHITE");

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Kami of Old Stone");
        harness.assertNotInGraveyard(player2, "Kami of Old Stone");
    }

    private Permanent flipBushi() {
        Permanent bushi = addCreatureReady(player1, new BushiTenderfoot());

        harness.setHand(player1, List.of(new IndomitableWill()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bushi.getId());
        addCreatureReady(player2, new IsamaruHoundOfKonda());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();
        return bushi;
    }
}
