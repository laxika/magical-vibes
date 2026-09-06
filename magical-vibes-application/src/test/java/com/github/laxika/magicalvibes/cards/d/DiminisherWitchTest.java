package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiminisherWitch.class, DarksteelRelic.class, GrizzlyBears.class})
class DiminisherWitchTest extends BaseCardTest {

    @Test
    void withoutBargainDoesNotCreateRole() {
        harness.setHand(player1, List.of(new DiminisherWitch()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cursed")).isEmpty();
    }

    @Test
    void withBargainSacrificesArtifactAndCreatesCursedRoleOnTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiminisherWitch()));
        addMana();

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, target.getId(), null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent role = findPermanents(player1, "Cursed").stream().findFirst().orElseThrow();
        assertThat(role.getCard().isToken()).isTrue();
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    void bargainCannotTargetOwnCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiminisherWitch()));
        addMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0,
                target.getId(), null, List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainCannotSacrificeCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiminisherWitch()));
        addMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0,
                target.getId(), null, List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
