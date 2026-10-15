package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaruMender.class, GrizzlyBears.class, Plains.class, Shock.class})
class DaruMenderTest extends BaseCardTest {

    @Test
    void turningFaceUpRegeneratesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mender = castFaceDown();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mender));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId())
                .contains(mender.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void turningFaceUpOnlyOffersCreatureTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent mender = castFaceDown();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mender));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(mender.getId())
                .doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, mender.getId());
        harness.passBothPriorities();
    }

    @Test
    void castingFaceUpDoesNotTriggerRegeneration() {
        harness.castFromHand(player1, new DaruMender(), "{W}");
        harness.passBothPriorities();

        Permanent mender = findPermanent(player1, "Daru Mender");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(mender.getRegenerationShield()).isZero();
    }

    @Test
    void canRegenerateItselfButShieldOnlyPreventsOneDestruction() {
        Permanent mender = castFaceDown();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mender));
        harness.handlePermanentChosen(player1, mender.getId());
        harness.passBothPriorities();

        assertThat(mender.getRegenerationShield()).isEqualTo(1);
        assertThat(mender.isTapped()).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, mender.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mender);
        assertThat(mender.isTapped()).isTrue();
        assertThat(mender.getMarkedDamage()).isZero();
        assertThat(mender.getRegenerationShield()).isZero();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, mender.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mender);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mender.getCard());
    }

    @Test
    void targetDestroyedInResponseDoesNotRegenerateAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mender = castFaceDown();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mender));
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(mender.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDown() {
        DaruMender card = new DaruMender();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Daru Mender");
    }
}
