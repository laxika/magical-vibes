package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CoastalPiracy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EllivereOfTheWildCourt.class, GrizzlyBears.class, CoastalPiracy.class, Forest.class})
class EllivereOfTheWildCourtTest extends BaseCardTest {

    @Test
    void entersAndAttachesVirtuousRoleToAnotherCreatureYouControl() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CoastalPiracy());
        castEllivere();

        Permanent ellivere = findPermanent(player1, "Ellivere of the Wild Court");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(ellivere.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Virtuous");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void virtuousRoleDrawsWhenEnchantedCreatureDealsCombatDamage() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castEllivere();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void attackingEllivereCreatesVirtuousRoleOnAnotherCreature() {
        Permanent ellivere = addCreatureReady(player1, new EllivereOfTheWildCourt());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(ellivere.getId());

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        Permanent role = findPermanent(player1, "Virtuous");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
    }

    private void castEllivere() {
        harness.setHand(player1, List.of(new EllivereOfTheWildCourt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
