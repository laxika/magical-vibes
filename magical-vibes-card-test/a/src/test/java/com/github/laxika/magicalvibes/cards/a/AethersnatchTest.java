package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.r.Recollect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aethersnatch.class, GrizzlyBears.class, LavaAxe.class, ConeOfFlame.class, Recollect.class})
class AethersnatchTest extends BaseCardTest {

    @Test
    void gainsControlOfCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayRetargetControlledSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lavaAxe.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
        harness.assertInGraveyard(player1, "Lava Axe");
        harness.assertNotInGraveyard(player2, "Lava Axe");
    }

    @Test
    void mayKeepOriginalTargetAndSpellReturnsToOwnersGraveyard() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, lavaAxe.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Lava Axe");
        harness.assertNotInGraveyard(player2, "Lava Axe");
    }

    @Test
    void mayChangeMoreThanOneTargetOfControlledSpell() {
        var first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ConeOfFlame cone = new ConeOfFlame();
        harness.setHand(player1, List.of(cone));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, cone.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, true);
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId());
    }

    @Test
    void mayRetargetToCardInNewControllersGraveyard() {
        GrizzlyBears originalTarget = new GrizzlyBears();
        GrizzlyBears newTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(originalTarget));
        harness.setGraveyard(player2, List.of(newTarget));
        Recollect recollect = new Recollect();
        harness.setHand(player1, List.of(recollect));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, originalTarget.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, recollect.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(newTarget.getId()).doesNotContain(originalTarget.getId());
        harness.handlePermanentChosen(player2, newTarget.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Recollect");
    }
}
