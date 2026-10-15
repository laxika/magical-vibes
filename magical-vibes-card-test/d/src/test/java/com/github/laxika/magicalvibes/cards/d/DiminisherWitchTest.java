package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HallarTheFirefletcher;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiminisherWitch.class, DarksteelRelic.class, GrizzlyBears.class, HallarTheFirefletcher.class})
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

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
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

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainCannotSacrificeCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiminisherWitch()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBargainWithoutAnOpposingCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new DiminisherWitch()));
        addMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Diminisher Witch");
        harness.assertInGraveyard(player1, "Darksteel Relic");
        assertThat(findPermanents(player1, "Cursed")).isEmpty();
    }

    @Test
    void newerCursedRoleReplacesTheSameControllersOlderRole() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiminisherWitch(), new DiminisherWitch()));

        for (int i = 0; i < 2; i++) {
            Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
            addMana();
            harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(findPermanents(player1, "Cursed")).hasSize(1);
        assertThat(findPermanents(player1, "Cursed").getFirst().getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void canBargainBySacrificingAnExistingCursedRole() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new DiminisherWitch(), new DiminisherWitch()));
        addMana();
        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent firstRole = findPermanents(player1, "Cursed").getFirst();

        addMana();
        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), firstRole.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cursed")).hasSize(1);
        assertThat(findPermanents(player1, "Cursed").getFirst().getId()).isNotEqualTo(firstRole.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void bargainingDoesNotTriggerAbilitiesForKickedSpells() {
        harness.addToBattlefield(player1, new HallarTheFirefletcher());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new DiminisherWitch()));
        harness.setLife(player2, 20);
        addMana();

        harness.castKickedInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Diminisher Witch");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
