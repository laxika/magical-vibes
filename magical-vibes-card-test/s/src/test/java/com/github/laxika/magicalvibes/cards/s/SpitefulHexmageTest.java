package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulHexmage.class, GrizzlyBears.class})
class SpitefulHexmageTest extends BaseCardTest {

    @Test
    void entersAndAttachesCursedRoleToTargetCreatureYouControl() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpitefulHexmage()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Cursed");
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void replacingCursedRolePutsTheOldRoleIntoTheGraveyard() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpitefulHexmage(), new SpitefulHexmage()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        castAndResolve(target);
        Permanent firstRole = findPermanent(player1, "Cursed");

        castAndResolve(target);

        assertThat(findPermanents(player1, "Cursed")).hasSize(1);
        assertThat(findPermanent(player1, "Cursed").getId()).isNotEqualTo(firstRole.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void canAttachTheRoleToItself() {
        harness.setHand(player1, List.of(new SpitefulHexmage()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent hexmage = findPermanent(player1, "Spiteful Hexmage");
        harness.handlePermanentChosen(player1, hexmage.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cursed").getAttachedTo()).isEqualTo(hexmage.getId());
        assertThat(gqs.getEffectivePower(gd, hexmage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hexmage)).isEqualTo(1);
    }

    @Test
    void cannotTargetAnOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new SpitefulHexmage());
        Permanent ownCreature = addCreatureReady(player1, new SpitefulHexmage());
        harness.setHand(player1, List.of(new SpitefulHexmage()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cursed").getAttachedTo()).isEqualTo(ownCreature.getId());
        assertThat(findPermanents(player2, "Cursed")).isEmpty();
    }

    @Test
    void cursedRolePreservesCounterBonuses() {
        Permanent target = addCreatureReady(player1, new SpitefulHexmage());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new SpitefulHexmage()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        castAndResolve(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    private void castAndResolve(Permanent target) {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
