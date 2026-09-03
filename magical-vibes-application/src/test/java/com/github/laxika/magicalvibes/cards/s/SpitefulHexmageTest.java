package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

    private void castAndResolve(Permanent target) {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
